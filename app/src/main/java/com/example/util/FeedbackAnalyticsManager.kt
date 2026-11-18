package com.example.util

import android.content.Context
import android.os.Build
import com.example.data.CloudFeedbackReport
import com.example.data.FirestoreChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class FeedbackCategory(val wireValue: String, val labelTr: String, val badgeEmoji: String) {
    FEEDBACK("FEEDBACK", "Geri Bildirim", "💬"),
    SUGGESTION("SUGGESTION", "Öneri", "💡"),
    BUG("BUG", "Hata Bildirimi", "🐞"),
    PERFORMANCE("PERFORMANCE", "Performans", "⚡");

    companion object {
        fun fromWire(value: String?): FeedbackCategory {
            return entries.firstOrNull { it.wireValue.equals(value?.trim(), ignoreCase = true) } ?: FEEDBACK
        }
    }
}

enum class FeedbackSeverity(val wireValue: String, val labelTr: String) {
    LOW("LOW", "Düşük"),
    MEDIUM("MEDIUM", "Orta"),
    HIGH("HIGH", "Yüksek"),
    CRITICAL("CRITICAL", "Kritik");

    companion object {
        fun fromWire(value: String?): FeedbackSeverity {
            return entries.firstOrNull { it.wireValue.equals(value?.trim(), ignoreCase = true) } ?: LOW
        }
    }
}

enum class FeedbackStatus(val wireValue: String, val labelTr: String) {
    OPEN("OPEN", "Açık"),
    IN_REVIEW("IN_REVIEW", "İnceleniyor"),
    RESOLVED("RESOLVED", "Çözüldü");

    fun nextCycle(): FeedbackStatus = when (this) {
        OPEN -> IN_REVIEW
        IN_REVIEW -> RESOLVED
        RESOLVED -> OPEN
    }

    companion object {
        fun fromWire(value: String?): FeedbackStatus {
            return entries.firstOrNull { it.wireValue.equals(value?.trim(), ignoreCase = true) } ?: OPEN
        }
    }
}

data class FeedbackReportItem(
    val id: String,
    val uid: String,
    val category: FeedbackCategory,
    val severity: FeedbackSeverity,
    val rating: Int,
    val moduleTag: String,
    val title: String,
    val description: String,
    val status: FeedbackStatus,
    val appVersion: String,
    val osVersion: String,
    val createdAtMs: Long,
    val syncedToCloud: Boolean
)

data class FeedbackAnalyticsSummary(
    val totalCount: Int,
    val averageRating: Double,
    val categoryCounts: Map<FeedbackCategory, Int>,
    val severityCounts: Map<FeedbackSeverity, Int>,
    val moduleCounts: Map<String, Int>,
    val openBugCount: Int,
    val resolvedCount: Int,
    val syncedCloudCount: Int,
    val topModule: String?
)

/**
 * KVKK-compliant (6698 Law Art. 4/5/10/11) Feedback, Suggestion & Bug Reporting Manager.
 * - Stores structured reports locally on device AND syncs with Firestore `/feedback_reports`.
 * - Zero intrusive PII collected: stores only user-entered content + non-identifying appVersion/OS SDK.
 * - Produces structured JSON/CSV exports and aggregated KPI metrics for downstream analysis.
 */
object FeedbackAnalyticsManager {
    private const val PREFS_NAME = "general_chat_feedback_analytics_prefs"
    private const val KEY_REPORTS_JSON = "feedback_reports_json_v1"

    val availableModuleTags = listOf(
        "Sohbet & Mesajlar",
        "GEN AI",
        "Görüntülü/Sesli Arama",
        "Konum & Galeri",
        "Ebeveyn Denetimi",
        "Ayarlar & KVKK",
        "Performans & 4K Arayüz"
    )

    private val _reports = MutableStateFlow<List<FeedbackReportItem>>(emptyList())
    val reports: StateFlow<List<FeedbackReportItem>> = _reports.asStateFlow()

    private val _summary = MutableStateFlow(computeAnalytics(emptyList()))
    val summary: StateFlow<FeedbackAnalyticsSummary> = _summary.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow<String?>(null)
    val lastSyncMessage: StateFlow<String?> = _lastSyncMessage.asStateFlow()

    fun init(context: Context) {
        val loaded = loadFromPrefs(context)
        publishList(loaded)
    }

    fun submitReport(
        context: Context,
        category: FeedbackCategory,
        severity: FeedbackSeverity,
        rating: Int,
        moduleTag: String,
        title: String,
        description: String,
        kvkkApproved: Boolean
    ): Result<FeedbackReportItem> {
        if (!kvkkApproved) {
            return Result.failure(IllegalArgumentException("KVKK Madde 5/10 bilgilendirme onayını işaretleyin."))
        }
        val cleanTitle = title.trim().take(120)
        val cleanDesc = description.trim().take(2000)
        if (cleanTitle.length < 3) {
            return Result.failure(IllegalArgumentException("Lütfen en az 3 karakterlik kısa bir başlık yazın."))
        }
        if (cleanDesc.length < 5) {
            return Result.failure(IllegalArgumentException("Lütfen analiz için en az 5 karakterlik açıklama girin."))
        }

        val currentUid = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        } ?: "local_device"

        val report = FeedbackReportItem(
            id = "fb_${UUID.randomUUID().toString().replace("-", "").take(16)}",
            uid = currentUid,
            category = category,
            severity = severity,
            rating = rating.coerceIn(1, 5),
            moduleTag = moduleTag.trim().ifBlank { "Genel" }.take(40),
            title = cleanTitle,
            description = cleanDesc,
            status = FeedbackStatus.OPEN,
            appVersion = resolveAppVersion(context),
            osVersion = resolveNonPiiOsVersion(),
            createdAtMs = System.currentTimeMillis(),
            syncedToCloud = false
        )

        val currentList = loadFromPrefs(context).toMutableList()
        currentList.add(0, report)
        saveToPrefs(context, currentList)
        publishList(currentList)
        return Result.success(report)
    }

    suspend fun submitAndSyncReport(
        context: Context,
        category: FeedbackCategory,
        severity: FeedbackSeverity,
        rating: Int,
        moduleTag: String,
        title: String,
        description: String,
        kvkkApproved: Boolean
    ): Result<FeedbackReportItem> {
        val localResult = submitReport(
            context = context,
            category = category,
            severity = severity,
            rating = rating,
            moduleTag = moduleTag,
            title = title,
            description = description,
            kvkkApproved = kvkkApproved
        )
        val item = localResult.getOrElse { return Result.failure(it) }

        val repo = try {
            FirestoreChatRepository.create(context)
        } catch (_: Exception) {
            null
        }

        if (repo == null || repo.currentUid() == null) {
            _lastSyncMessage.value = "Cihaz analiz deposuna kaydedildi (bulut için Google oturumu isteğe bağlı)."
            return Result.success(item)
        }

        _isSyncing.value = true
        return try {
            val cloudRes = repo.submitFeedbackReport(item.toCloudReport())
            if (cloudRes.isSuccess) {
                val synced = item.copy(
                    uid = cloudRes.getOrNull()?.uid ?: item.uid,
                    syncedToCloud = true
                )
                updateLocalItem(context, synced)
                _lastSyncMessage.value = "Firebase Bulut Analiz Deposu'na kaydedildi."
                Result.success(synced)
            } else {
                _lastSyncMessage.value = "Yerel olarak kaydedildi; bulut eşitlemesi daha sonra denenebilir."
                Result.success(item)
            }
        } catch (_: Exception) {
            _lastSyncMessage.value = "Yerel olarak kaydedildi (çevrimdışı)."
            Result.success(item)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun syncWithCloud(context: Context): Result<Int> {
        val repo = try {
            FirestoreChatRepository.create(context)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val uid = repo.currentUid()
            ?: return Result.failure(IllegalStateException("Bulut eşitlemesi için Google ile giriş yapın."))

        _isSyncing.value = true
        return try {
            // 1. Push any unsynced local reports belonging to user or local_device
            var localList = loadFromPrefs(context).toMutableList()
            localList.forEachIndexed { idx, item ->
                if (!item.syncedToCloud) {
                    val pushRes = repo.submitFeedbackReport(item.copy(uid = uid).toCloudReport())
                    if (pushRes.isSuccess) {
                        localList[idx] = item.copy(uid = uid, syncedToCloud = true)
                    }
                }
            }

            // 2. Pull cloud reports and merge by ID
            val cloudReports = repo.fetchMyFeedbackReports().getOrElse { return Result.failure(it) }
            val mergedById = LinkedHashMap<String, FeedbackReportItem>()
            cloudReports.forEach { cloud ->
                mergedById[cloud.id] = cloud.toItem()
            }
            localList.forEach { local ->
                if (!mergedById.containsKey(local.id)) {
                    mergedById[local.id] = local
                }
            }
            val finalSorted = mergedById.values.sortedByDescending { it.createdAtMs }
            saveToPrefs(context, finalSorted)
            publishList(finalSorted)
            _lastSyncMessage.value = "${finalSorted.size} rapor Firebase ile eşitlendi."
            Result.success(finalSorted.size)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun cycleReportStatus(context: Context, reportId: String): Result<FeedbackStatus> {
        val list = loadFromPrefs(context).toMutableList()
        val idx = list.indexOfFirst { it.id == reportId }
        if (idx < 0) return Result.failure(IllegalArgumentException("Rapor bulunamadı."))
        val nextStatus = list[idx].status.nextCycle()
        val updated = list[idx].copy(status = nextStatus)
        list[idx] = updated
        saveToPrefs(context, list)
        publishList(list)

        if (updated.syncedToCloud) {
            try {
                val repo = FirestoreChatRepository.create(context)
                if (repo.currentUid() != null) {
                    repo.updateFeedbackStatus(reportId = reportId, newStatus = nextStatus.wireValue)
                }
            } catch (_: Exception) {
            }
        }
        return Result.success(nextStatus)
    }

    suspend fun deleteReport(context: Context, reportId: String): Result<Unit> {
        val list = loadFromPrefs(context).filterNot { it.id == reportId }
        saveToPrefs(context, list)
        publishList(list)
        try {
            val repo = FirestoreChatRepository.create(context)
            if (repo.currentUid() != null) {
                repo.deleteFeedbackReport(reportId)
            }
        } catch (_: Exception) {
        }
        return Result.success(Unit)
    }

    fun clearLocalFeedbackHistory(context: Context) {
        saveToPrefs(context, emptyList())
        publishList(emptyList())
    }

    fun computeAnalytics(items: List<FeedbackReportItem>): FeedbackAnalyticsSummary {
        if (items.isEmpty()) {
            return FeedbackAnalyticsSummary(
                totalCount = 0,
                averageRating = 0.0,
                categoryCounts = FeedbackCategory.entries.associateWith { 0 },
                severityCounts = FeedbackSeverity.entries.associateWith { 0 },
                moduleCounts = emptyMap(),
                openBugCount = 0,
                resolvedCount = 0,
                syncedCloudCount = 0,
                topModule = null
            )
        }
        val catCounts = FeedbackCategory.entries.associateWith { cat -> items.count { it.category == cat } }
        val sevCounts = FeedbackSeverity.entries.associateWith { sev -> items.count { it.severity == sev } }
        val modCounts = items.groupingBy { it.moduleTag }.eachCount()
        val avg = items.map { it.rating }.average()
        val openBugs = items.count {
            (it.category == FeedbackCategory.BUG || it.category == FeedbackCategory.PERFORMANCE) &&
                it.status != FeedbackStatus.RESOLVED
        }
        val resolved = items.count { it.status == FeedbackStatus.RESOLVED }
        val synced = items.count { it.syncedToCloud }
        val topMod = modCounts.maxByOrNull { it.value }?.key
        return FeedbackAnalyticsSummary(
            totalCount = items.size,
            averageRating = avg,
            categoryCounts = catCounts,
            severityCounts = sevCounts,
            moduleCounts = modCounts,
            openBugCount = openBugs,
            resolvedCount = resolved,
            syncedCloudCount = synced,
            topModule = topMod
        )
    }

    fun exportAnalyticsJson(items: List<FeedbackReportItem> = _reports.value): String {
        val stats = computeAnalytics(items)
        val root = JSONObject()
        root.put("schemaVersion", "1.0")
        root.put("app", "GENeral Chat")
        root.put("exportedAtIso", isoFormat(System.currentTimeMillis()))
        root.put("kvkkCompliance", "6698 Law Minimized Telemetry - No PII")

        val summaryObj = JSONObject().apply {
            put("totalCount", stats.totalCount)
            put("averageRating", String.format(Locale.US, "%.2f", stats.averageRating).toDouble())
            put("openBugOrPerfCount", stats.openBugCount)
            put("resolvedCount", stats.resolvedCount)
            put("syncedCloudCount", stats.syncedCloudCount)
            put("topReportedModule", stats.topModule ?: "")
            put("categoryBreakdown", JSONObject().apply {
                stats.categoryCounts.forEach { (k, v) -> put(k.wireValue, v) }
            })
            put("severityBreakdown", JSONObject().apply {
                stats.severityCounts.forEach { (k, v) -> put(k.wireValue, v) }
            })
            put("moduleBreakdown", JSONObject().apply {
                stats.moduleCounts.forEach { (k, v) -> put(k, v) }
            })
        }
        root.put("analyticsSummary", summaryObj)

        val rows = JSONArray()
        items.forEach { item ->
            rows.put(item.toJson())
        }
        root.put("reports", rows)
        return root.toString(2)
    }

    fun exportAnalyticsCsv(items: List<FeedbackReportItem> = _reports.value): String {
        val sb = StringBuilder()
        sb.appendLine("id,createdAtIso,category,severity,rating,moduleTag,status,title,description,appVersion,osVersion,syncedToCloud")
        items.forEach { item ->
            val fields = listOf(
                item.id,
                isoFormat(item.createdAtMs),
                item.category.wireValue,
                item.severity.wireValue,
                item.rating.toString(),
                item.moduleTag,
                item.status.wireValue,
                item.title,
                item.description,
                item.appVersion,
                item.osVersion,
                item.syncedToCloud.toString()
            )
            sb.appendLine(fields.joinToString(",") { csvEscape(it) })
        }
        return sb.toString()
    }

    private fun csvEscape(raw: String): String {
        val normalized = raw.replace("\r\n", " ").replace("\n", " ")
        val escaped = normalized.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun isoFormat(epochMs: Long): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(epochMs))
    }

    private fun resolveAppVersion(context: Context): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName?.takeIf { it.isNotBlank() }?.take(32) ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    private fun resolveNonPiiOsVersion(): String {
        return "Android SDK ${Build.VERSION.SDK_INT}".take(48)
    }

    private fun publishList(items: List<FeedbackReportItem>) {
        val sorted = items.sortedByDescending { it.createdAtMs }
        _reports.value = sorted
        _summary.value = computeAnalytics(sorted)
    }

    private fun updateLocalItem(context: Context, updated: FeedbackReportItem) {
        val list = loadFromPrefs(context).toMutableList()
        val idx = list.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            list[idx] = updated
        } else {
            list.add(0, updated)
        }
        saveToPrefs(context, list)
        publishList(list)
    }

    private fun saveToPrefs(context: Context, items: List<FeedbackReportItem>) {
        val arr = JSONArray()
        items.take(200).forEach { arr.put(it.toJson()) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REPORTS_JSON, arr.toString())
            .apply()
    }

    private fun loadFromPrefs(context: Context): List<FeedbackReportItem> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REPORTS_JSON, null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        FeedbackReportItem(
                            id = obj.optString("id", ""),
                            uid = obj.optString("uid", "local_device"),
                            category = FeedbackCategory.fromWire(obj.optString("category")),
                            severity = FeedbackSeverity.fromWire(obj.optString("severity")),
                            rating = obj.optInt("rating", 5).coerceIn(1, 5),
                            moduleTag = obj.optString("moduleTag", "Genel"),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            status = FeedbackStatus.fromWire(obj.optString("status")),
                            appVersion = obj.optString("appVersion", "1.0"),
                            osVersion = obj.optString("osVersion", "Android"),
                            createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis()),
                            syncedToCloud = obj.optBoolean("syncedToCloud", false)
                        )
                    )
                }
            }.filter { it.id.isNotBlank() && it.title.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun FeedbackReportItem.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("uid", uid)
        put("category", category.wireValue)
        put("severity", severity.wireValue)
        put("rating", rating)
        put("moduleTag", moduleTag)
        put("title", title)
        put("description", description)
        put("status", status.wireValue)
        put("appVersion", appVersion)
        put("osVersion", osVersion)
        put("createdAtMs", createdAtMs)
        put("createdAtIso", isoFormat(createdAtMs))
        put("syncedToCloud", syncedToCloud)
    }

    private fun FeedbackReportItem.toCloudReport(): CloudFeedbackReport = CloudFeedbackReport(
        id = id,
        uid = uid,
        category = category.wireValue,
        severity = severity.wireValue,
        rating = rating,
        moduleTag = moduleTag,
        title = title,
        description = description,
        status = status.wireValue,
        appVersion = appVersion,
        osVersion = osVersion,
        clientTimestampMs = createdAtMs
    )

    private fun CloudFeedbackReport.toItem(): FeedbackReportItem = FeedbackReportItem(
        id = id,
        uid = uid,
        category = FeedbackCategory.fromWire(category),
        severity = FeedbackSeverity.fromWire(severity),
        rating = rating,
        moduleTag = moduleTag,
        title = title,
        description = description,
        status = FeedbackStatus.fromWire(status),
        appVersion = appVersion,
        osVersion = osVersion,
        createdAtMs = clientTimestampMs,
        syncedToCloud = true
    )
}
