package com.metrolist.music.ui.component

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.ui.theme.SpotifyFontFamily

private val SheetColor = Color(0xFF282828)
private val SpotifyGreen = Color(0xFF1DB954)
private val SpotifyGray = Color(0xFFB3B3B3)

/** Bluetooth outputs the user can route media audio to (SCO is call audio, so it is left out). */
private fun AudioDeviceInfo.isBluetoothOutput(): Boolean =
    type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
        (Build.VERSION.SDK_INT >= 28 && type == AudioDeviceInfo.TYPE_HEARING_AID) ||
        (
            Build.VERSION.SDK_INT >= 31 &&
                (type == AudioDeviceInfo.TYPE_BLE_HEADSET || type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
        )

private fun AudioDeviceInfo.isPhoneSpeaker(): Boolean = type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER

private fun AudioManager.queryOutputs(): List<AudioDeviceInfo> =
    getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        .filter { it.isSink && (it.isBluetoothOutput() || it.isPhoneSpeaker()) }

/** Live list of the phone speaker + connected Bluetooth outputs. */
@Composable
internal fun rememberAudioOutputs(): State<List<AudioDeviceInfo>> {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val outputs = remember { mutableStateOf(audioManager.queryOutputs()) }
    DisposableEffect(audioManager) {
        val callback =
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    outputs.value = audioManager.queryOutputs()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    outputs.value = audioManager.queryOutputs()
                }
            }
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        onDispose { audioManager.unregisterAudioDeviceCallback(callback) }
    }
    return outputs
}

/**
 * The local output audio is going to right now: the user's pick if it is still connected,
 * otherwise what Android does by default (a connected Bluetooth device, else the phone speaker).
 * Null while casting.
 */
internal fun activeLocalOutput(
    outputs: List<AudioDeviceInfo>,
    preferredId: Int?,
    isCasting: Boolean,
): AudioDeviceInfo? {
    if (isCasting) return null
    return outputs.firstOrNull { it.id == preferredId }
        ?: outputs.firstOrNull { it.isBluetoothOutput() }
        ?: outputs.firstOrNull { it.isPhoneSpeaker() }
}

internal fun AudioDeviceInfo.displayName(context: Context): String =
    if (isPhoneSpeaker()) {
        context.getString(R.string.audio_output_this_phone)
    } else {
        productName?.toString()?.takeIf { it.isNotBlank() } ?: context.getString(R.string.audio_output_bluetooth)
    }

internal fun AudioDeviceInfo.isBluetooth(): Boolean = isBluetoothOutput()

/** Cast devices currently visible on the network; discovery runs only while this is composed. */
@Composable
private fun rememberCastRoutes(): State<List<MediaRouter.RouteInfo>> {
    val context = LocalContext.current
    val routes = remember { mutableStateOf<List<MediaRouter.RouteInfo>>(emptyList()) }
    DisposableEffect(Unit) {
        val selector = runCatching { CastContext.getSharedInstance(context).mergedSelector }.getOrNull()
        if (selector == null) {
            return@DisposableEffect onDispose { }
        }
        val router = MediaRouter.getInstance(context)

        fun refresh() {
            routes.value = router.routes.filter { !it.isDefault && it.isEnabled && it.matchesSelector(selector) }
        }

        val callback =
            object : MediaRouter.Callback() {
                override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()

                override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()

                override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
            }
        router.addCallback(selector, callback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)
        refresh()
        onDispose { router.removeCallback(callback) }
    }
    return routes
}

/**
 * Spotify 2018 "Connect to a device" sheet: this phone, connected Bluetooth devices, then Cast devices.
 * Picking a phone/Bluetooth row routes the player's audio there (and ends any Cast session);
 * picking a Cast row starts casting.
 */
@Composable
fun AudioOutputSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val castHandler = LocalPlayerConnection.current?.service?.castConnectionHandler
    val isCasting by castHandler?.isCasting?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val castDeviceName by castHandler?.castDeviceName?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<String?>(null) }
    val preferredId by castHandler?.preferredAudioDeviceId?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<Int?>(null) }
    val outputs by rememberAudioOutputs()
    val castRoutes by rememberCastRoutes()
    val active = activeLocalOutput(outputs, preferredId, isCasting)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.apply {
                setGravity(Gravity.BOTTOM)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setWindowAnimations(R.style.SpotifyCastSheetAnimation)
                setDimAmount(0.6f)
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(SheetColor)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.audio_output_title),
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = SpotifyFontFamily,
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp),
            )

            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // This phone, then Bluetooth devices
                outputs.filter { it.isPhoneSpeaker() }.forEach { device ->
                    OutputRow(
                        iconRes = R.drawable.spotify_output_phone,
                        title = device.displayName(context),
                        subtitle = null,
                        selected = active?.id == device.id,
                    ) {
                        if (isCasting) castHandler?.disconnect()
                        castHandler?.setPreferredAudioDevice(device)
                        onDismiss()
                    }
                }
                outputs.filter { it.isBluetoothOutput() }.forEach { device ->
                    OutputRow(
                        iconRes = R.drawable.bluetooth,
                        title = device.displayName(context),
                        subtitle = stringResource(R.string.audio_output_bluetooth),
                        selected = active?.id == device.id,
                    ) {
                        if (isCasting) castHandler?.disconnect()
                        castHandler?.setPreferredAudioDevice(device)
                        onDismiss()
                    }
                }

                // Cast devices below
                castRoutes.forEach { route ->
                    OutputRow(
                        iconRes = R.drawable.cast,
                        title = route.name,
                        subtitle = stringResource(R.string.audio_output_cast),
                        selected = isCasting && castDeviceName == route.name,
                    ) {
                        route.select()
                        onDismiss()
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OutputRow(
    iconRes: Int,
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 16.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (selected) SpotifyGreen else SpotifyGray,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (selected) SpotifyGreen else Color.White,
                fontSize = 16.sp,
                fontFamily = SpotifyFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (selected) SpotifyGreen else SpotifyGray,
                    fontSize = 12.sp,
                    fontFamily = SpotifyFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
