package com.example.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LinkedDeviceItem(
    val id: String,
    val deviceName: String,
    val platform: String,
    val pairingCode: String,
    val linkedAt: String,
    val isActive: Boolean = true
)

/**
 * Manages Linked Devices (Bağlı Cihazlar) for transferring or companion-syncing
 * GENeral Chat sessions across Web (Chrome/Firefox/Safari), iOS, Tablets, and Desktop PCs.
 */
object LinkedDevicesManager {
    private const val PREFS = "general_chat_linked_devices"
    private var appContext: Context? = null

    private val _devices = MutableStateFlow<List<LinkedDeviceItem>>(emptyList())
    val devices: StateFlow<List<LinkedDeviceItem>> = _devices.asStateFlow()

    private val _activePairingCode = MutableStateFlow(generatePairingCode())
    val activePairingCode: StateFlow<String> = _activePairingCode.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedCode = prefs.getString("pairing_code", null)
        if (!savedCode.isNullOrBlank()) {
            _activePairingCode.value = savedCode
        } else {
            prefs.edit().putString("pairing_code", _activePairingCode.value).apply()
        }
        val raw = prefs.getString("devices_json", null)
        if (!raw.isNullOrBlank()) {
            _devices.value = parseDevices(raw)
        }
    }

    fun regeneratePairingCode(): String {
        val code = generatePairingCode()
        _activePairingCode.value = code
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putString("pairing_code", code)?.apply()
        publishCodeToCloud(code)
        return code
    }

    fun linkDevice(
        deviceName: String,
        platform: String,
        codeOverride: String? = null
    ): LinkedDeviceItem {
        val item = LinkedDeviceItem(
            id = UUID.randomUUID().toString(),
            deviceName = deviceName.trim().ifBlank { "Web Tarayıcı" },
            platform = platform.trim().ifBlank { "Web" },
            pairingCode = codeOverride ?: _activePairingCode.value,
            linkedAt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date()),
            isActive = true
        )
        val updated = listOf(item) + _devices.value.filterNot { it.id == item.id }
        _devices.value = updated
        saveDevices(updated)
        publishCodeToCloud(item.pairingCode, linkedDevice = item)
        return item
    }

    fun unlinkDevice(deviceId: String) {
        val updated = _devices.value.filterNot { it.id == deviceId }
        _devices.value = updated
        saveDevices(updated)
    }

    fun clearAllDevices() {
        _devices.value = emptyList()
        saveDevices(emptyList())
    }

    private fun publishCodeToCloud(code: String, linkedDevice: LinkedDeviceItem? = null) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val ctx = appContext ?: return
            val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
            val dbId = if (resId != 0) ctx.getString(resId) else ""
            if (dbId.isBlank()) return
            val db = FirebaseFirestore.getInstance(dbId)
            val payload = hashMapOf<String, Any>(
                "code" to code,
                "ownerUid" to uid,
                "status" to if (linkedDevice != null) "PAIRED" else "WAITING",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (linkedDevice != null) {
                payload["deviceName"] = linkedDevice.deviceName
                payload["platform"] = linkedDevice.platform
            }
            db.collection("device_links").document(code).set(payload, SetOptions.merge())
        } catch (_: Exception) {
        }
    }

    private fun saveDevices(list: List<LinkedDeviceItem>) {
        val arr = JSONArray()
        list.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("name", d.deviceName)
                put("platform", d.platform)
                put("code", d.pairingCode)
                put("at", d.linkedAt)
                put("active", d.isActive)
            })
        }
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putString("devices_json", arr.toString())?.apply()
    }

    private fun parseDevices(raw: String): List<LinkedDeviceItem> {
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LinkedDeviceItem(
                    id = obj.optString("id"),
                    deviceName = obj.optString("name"),
                    platform = obj.optString("platform"),
                    pairingCode = obj.optString("code"),
                    linkedAt = obj.optString("at"),
                    isActive = obj.optBoolean("active", true)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "GC-" + (1..6).map { chars.random() }.joinToString("")
    }
}
