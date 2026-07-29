package com.lostf1sh.pixelplayeross.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.lostf1sh.pixelplayeross.data.preferences.AppThemeMode

enum class AppSkin { DEFAULT, DEEZER }

val LocalAppSkin = staticCompositionLocalOf { AppSkin.DEFAULT }

fun resolveAppSkin(appThemeMode: String): AppSkin =
    if (appThemeMode == AppThemeMode.DEEZER) AppSkin.DEEZER else AppSkin.DEFAULT

fun resolveUseDarkTheme(appThemeMode: String, systemDarkTheme: Boolean): Boolean =
    when (appThemeMode) {
        AppThemeMode.DARK, AppThemeMode.DEEZER -> true
        AppThemeMode.LIGHT -> false
        else -> systemDarkTheme
    }
