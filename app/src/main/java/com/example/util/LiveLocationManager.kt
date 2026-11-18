package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.CloudLiveLocation
import com.example.data.FirestoreChatRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Official real-time GPS location sharing powered by FusedLocationProviderClient
 * with Android LocationManager fallback and Firestore multi-device synchronization.
 */
object LiveLocationManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeLocalSessions = MutableStateFlow<Map<String, CloudLiveLocation>>(emptyMap())
    val activeLocalSessions: StateFlow<Map<String, CloudLiveLocation>> = _activeLocalSessions.asStateFlow()

    private val activeCallbacks = mutableMapOf<String, () -> Unit>()

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchFreshLocationSnapshot(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        val fused = runCatching { LocationServices.getFusedLocationProviderClient(context) }.getOrNull()
        if (fused != null) {
            val fresh = withTimeoutOrNull(4_500L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }
            if (fresh != null) return fresh

            val last = withTimeoutOrNull(1_500L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fused.lastLocation
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                }
            }
            if (last != null) return last
        }

        return runCatching {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    fun startLiveLocationSession(
        context: Context,
        locationId: String,
        conversationId: String,
        senderId: String,
        senderName: String,
        participantIds: List<String>,
        initialLatitude: Double,
        initialLongitude: Double,
        initialAccuracy: Double,
        label: String,
        durationMinutes: Int // 0 = manual stop
    ) {
        stopLiveLocationSession(context, conversationId, locationId, syncStopToCloud = false)
        val expiresAtMs = if (durationMinutes > 0) {
            System.currentTimeMillis() + durationMinutes * 60_000L
        } else 0L

        val initialSession = CloudLiveLocation(
            id = locationId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            participantIds = participantIds,
            latitude = initialLatitude.coerceIn(-90.0, 90.0),
            longitude = initialLongitude.coerceIn(-180.0, 180.0),
            accuracyMeters = initialAccuracy.coerceIn(0.0, 100000.0),
            label = label.ifBlank { "Canlı Konum" },
            isActive = true,
            expiresAtMs = expiresAtMs,
            updatedAtMs = System.currentTimeMillis()
        )
        publishSessionUpdate(context, initialSession)

        val isSelfOrAi = conversationId == SelfChatStorage.SELF_NOTES_CONVERSATION_ID ||
            conversationId == "conv_gemini_ai"

        val stopActions = mutableListOf<() -> Unit>()

        if (hasLocationPermission(context)) {
            val fused = runCatching { LocationServices.getFusedLocationProviderClient(context) }.getOrNull()
            if (fused != null) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
                    .setMinUpdateIntervalMillis(3_000L)
                    .build()
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val loc = result.lastLocation ?: return
                        if (expiresAtMs > 0L && System.currentTimeMillis() > expiresAtMs) {
                            stopLiveLocationSession(context, conversationId, locationId, syncStopToCloud = !isSelfOrAi)
                            return
                        }
                        publishSessionUpdate(
                            context,
                            initialSession.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                accuracyMeters = loc.accuracy.toDouble(),
                                updatedAtMs = System.currentTimeMillis()
                            )
                        )
                    }
                }
                runCatching {
                    fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
                    stopActions.add { runCatching { fused.removeLocationUpdates(callback) } }
                }
            } else {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        if (expiresAtMs > 0L && System.currentTimeMillis() > expiresAtMs) {
                            stopLiveLocationSession(context, conversationId, locationId, syncStopToCloud = !isSelfOrAi)
                            return
                        }
                        publishSessionUpdate(
                            context,
                            initialSession.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                accuracyMeters = loc.accuracy.toDouble(),
                                updatedAtMs = System.currentTimeMillis()
                            )
                        )
                    }
                    @Deprecated("Deprecated in Android SDK")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                runCatching {
                    lm?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5_000L, 2f, listener, Looper.getMainLooper())
                    stopActions.add { runCatching { lm?.removeUpdates(listener) } }
                }
            }
        }

        if (expiresAtMs > 0L) {
            val expiryJob = scope.launch {
                val remaining = (expiresAtMs - System.currentTimeMillis()).coerceAtLeast(1_000L)
                delay(remaining)
                stopLiveLocationSession(context, conversationId, locationId, syncStopToCloud = !isSelfOrAi)
            }
            stopActions.add { expiryJob.cancel() }
        }

        activeCallbacks[locationId] = {
            stopActions.forEach { runCatching { it() } }
        }
    }

    fun stopLiveLocationSession(
        context: Context,
        conversationId: String,
        locationId: String,
        syncStopToCloud: Boolean = true
    ) {
        activeCallbacks.remove(locationId)?.invoke()
        val existing = _activeLocalSessions.value[locationId]
        if (existing != null) {
            val updated = existing.copy(isActive = false, updatedAtMs = System.currentTimeMillis())
            _activeLocalSessions.value = _activeLocalSessions.value + (locationId to updated)
        }
        val isSelfOrAi = conversationId == SelfChatStorage.SELF_NOTES_CONVERSATION_ID ||
            conversationId == "conv_gemini_ai"
        if (syncStopToCloud && !isSelfOrAi) {
            scope.launch(Dispatchers.IO) {
                FirestoreChatRepository.create(context).stopLiveLocation(conversationId, locationId)
            }
        }
    }

    fun stopAllActiveSessions(context: Context) {
        val currentIds = activeCallbacks.keys.toList()
        currentIds.forEach { locId ->
            val session = _activeLocalSessions.value[locId]
            stopLiveLocationSession(
                context = context,
                conversationId = session?.conversationId ?: "",
                locationId = locId,
                syncStopToCloud = session != null
            )
        }
    }

    fun openInMaps(context: Context, latitude: Double, longitude: Double, label: String) {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun publishSessionUpdate(context: Context, session: CloudLiveLocation) {
        _activeLocalSessions.value = _activeLocalSessions.value + (session.id to session)
        val isSelfOrAi = session.conversationId == SelfChatStorage.SELF_NOTES_CONVERSATION_ID ||
            session.conversationId == "conv_gemini_ai"
        if (!isSelfOrAi) {
            scope.launch(Dispatchers.IO) {
                FirestoreChatRepository.create(context).upsertLiveLocation(session)
            }
        }
    }
}
