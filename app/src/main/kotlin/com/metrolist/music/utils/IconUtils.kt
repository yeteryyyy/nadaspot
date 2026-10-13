package com.metrolist.music.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconUtils {
    /**
     * Enables exactly one launcher alias, picked by icon style (dynamic/static) and launcher name
     * (Metrolist/Spotify); the other aliases are disabled.
     */
    fun setIcon(
        context: Context,
        dynamicIcon: Boolean,
        spotifyName: Boolean = false,
    ) {
        val pm = context.packageManager
        val enabledName =
            when {
                spotifyName && dynamicIcon -> "MainActivitySpotify"
                spotifyName -> "MainActivitySpotifyStatic"
                dynamicIcon -> "MainActivityAlias"
                else -> "MainActivityStatic"
            }
        val all = listOf("MainActivityAlias", "MainActivityStatic", "MainActivitySpotify", "MainActivitySpotifyStatic")
        // Enable the new one first so there is always a launcher entry
        pm.setComponentEnabledSetting(
            ComponentName(context, "com.metrolist.music.$enabledName"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
        all.filter { it != enabledName }.forEach {
            pm.setComponentEnabledSetting(
                ComponentName(context, "com.metrolist.music.$it"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
