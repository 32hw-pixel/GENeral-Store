package com.example.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class WebResearchResult(
    val query: String,
    val summary: String,
    val sourceUrls: List<String> = emptyList(),
    val succeeded: Boolean = false
)

/**
 * Free, zero-API-key live internet research helper using public HTTPS JSON endpoints
 * (Wikipedia REST API + DuckDuckGo Instant Answer API) executed concurrently with strict timeouts,
 * HTTP connection pooling, and a short TTL cache so GEN AI stays responsive and degrades cleanly offline.
 */
object WebResearchHelper {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L
    private const val MAX_CACHE_ENTRIES = 32

    private data class CachedResearch(
        val result: WebResearchResult,
        val timestampMs: Long
    )

    private val cache = ConcurrentHashMap<String, CachedResearch>()

    private val client: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 16
            maxRequestsPerHost = 8
        }
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(5, 3, TimeUnit.MINUTES))
            .connectTimeout(1200, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .callTimeout(1800, TimeUnit.MILLISECONDS)
            .build()
    }

    fun shouldResearchOnline(prompt: String): Boolean {
        val lower = prompt.lowercase(Locale.getDefault()).trim()
        if (lower.length < 4) return false
        val localOnlyHints = listOf(
            "en son kime", "bir mesajı arıyorum", "bir mesaji ariyorum",
            "kimle konuştum", "kimseye yaz", "sohbet geçmişi",
            "merhaba", "selam", "nasılsın", "naber", "kimsin", "adın ne",
            "kotlin", "python", "java ", "compose", "kod yaz", "hesapla"
        )
        if (localOnlyHints.any { lower.contains(it) }) return false
        val researchTriggers = listOf(
            "nedir", "kimdir", "ne zaman", "nerede", "başkent", "nüfus",
            "wikipedia", "araştır", "webden bul", "internetten bak",
            "hakkında bilgi", "what is", "who is", "explain", "was ist",
            "güncel haber", "tarihçe"
        )
        return researchTriggers.any { lower.contains(it) }
    }

    suspend fun searchLive(prompt: String): WebResearchResult = withContext(Dispatchers.IO) {
        val cleanTopic = extractSearchTopic(prompt)
        if (cleanTopic.isBlank()) {
            return@withContext WebResearchResult(query = prompt, summary = "", succeeded = false)
        }

        val cacheKey = cleanTopic.lowercase(Locale.ROOT)
        val now = System.currentTimeMillis()
        cache[cacheKey]?.let { cached ->
            if (now - cached.timestampMs <= CACHE_TTL_MS) {
                return@withContext cached.result
            } else {
                cache.remove(cacheKey)
            }
        }

        val combinedResult = try {
            withTimeoutOrNull(1800L) {
                coroutineScope {
                    val trDeferred = async { queryWikipedia(cleanTopic, "tr") }
                    val enDeferred = async { queryWikipedia(cleanTopic, "en") }
                    val ddgDeferred = async { queryDuckDuckGo(cleanTopic) }

                    val trWiki = trDeferred.await()
                    val enWiki = if (trWiki == null) enDeferred.await() else null
                    val ddg = ddgDeferred.await()

                    val summaries = mutableListOf<String>()
                    val urls = mutableListOf<String>()

                    val chosenWiki = trWiki ?: enWiki
                    if (chosenWiki != null) {
                        summaries.add(chosenWiki.first)
                        urls.add(chosenWiki.second)
                    }
                    if (ddg != null) {
                        if (summaries.isEmpty() || ddg.first.length > 40) {
                            summaries.add(ddg.first)
                        }
                        if (ddg.second.isNotBlank() && ddg.second !in urls) {
                            urls.add(ddg.second)
                        }
                    }

                    if (summaries.isEmpty()) {
                        null
                    } else {
                        WebResearchResult(
                            query = cleanTopic,
                            summary = summaries.joinToString("\n\n").take(1400),
                            sourceUrls = urls,
                            succeeded = true
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

        val finalResult = combinedResult
            ?: WebResearchResult(query = cleanTopic, summary = "", sourceUrls = emptyList(), succeeded = false)
        if (finalResult.succeeded) {
            if (cache.size >= MAX_CACHE_ENTRIES) {
                cache.clear()
            }
            cache[cacheKey] = CachedResearch(finalResult, System.currentTimeMillis())
        }
        finalResult
    }

    private fun extractSearchTopic(prompt: String): String {
        val stripped = prompt
            .replace(Regex("(?i)\\b(nedir|kimdir|ne demek|hakkında bilgi ver|araştır|webden bul|internetten bak|bana anlat|explain|what is|who is|was ist)\\b"), " ")
            .replace(Regex("[?.,!\"']"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return stripped.take(80)
    }

    private fun queryWikipedia(topic: String, lang: String): Pair<String, String>? {
        return try {
            val encodedTitle = URLEncoder.encode(topic.replace(" ", "_"), "UTF-8")
            val url = "https://$lang.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "GENeralChat/7.7 (Android)")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JSONObject(body)
                val extract = json.optString("extract").trim()
                val type = json.optString("type")
                if (extract.isBlank() || type == "disambiguation") return null
                val pageUrl = json.optJSONObject("content_urls")
                    ?.optJSONObject("desktop")
                    ?.optString("page")
                    ?.takeIf { it.isNotBlank() }
                    ?: "https://$lang.wikipedia.org/wiki/$encodedTitle"
                extract.take(800) to pageUrl
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun queryDuckDuckGo(topic: String): Pair<String, String>? {
        return try {
            val encoded = URLEncoder.encode(topic, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "GENeralChat/7.7 (Android)")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JSONObject(body)
                val abs = json.optString("AbstractText").trim()
                val ans = json.optString("Answer").trim()
                val abstractUrl = json.optString("AbstractURL").trim()
                val combined = listOf(ans, abs).filter { it.isNotBlank() }.joinToString(" — ")
                if (combined.isBlank()) return null
                combined.take(700) to abstractUrl
            }
        } catch (_: Exception) {
            null
        }
    }
}
