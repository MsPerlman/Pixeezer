package com.lostf1sh.pixelplayeross.ui.theme

import com.lostf1sh.pixelplayeross.data.preferences.AppThemeMode
import org.junit.Assert.*
import org.junit.Test

class SkinTest {

    @Test
    fun `resolveAppSkin returns DEEZER only for the deezer mode`() {
        assertEquals(AppSkin.DEEZER, resolveAppSkin(AppThemeMode.DEEZER))
        assertEquals(AppSkin.DEFAULT, resolveAppSkin(AppThemeMode.LIGHT))
        assertEquals(AppSkin.DEFAULT, resolveAppSkin(AppThemeMode.DARK))
        assertEquals(AppSkin.DEFAULT, resolveAppSkin(AppThemeMode.FOLLOW_SYSTEM))
    }

    @Test
    fun `resolveUseDarkTheme forces dark for deezer regardless of system theme`() {
        assertTrue(resolveUseDarkTheme(AppThemeMode.DEEZER, systemDarkTheme = false))
        assertTrue(resolveUseDarkTheme(AppThemeMode.DEEZER, systemDarkTheme = true))
    }

    @Test
    fun `resolveUseDarkTheme keeps existing light-dark-system behavior`() {
        assertTrue(resolveUseDarkTheme(AppThemeMode.DARK, systemDarkTheme = false))
        assertFalse(resolveUseDarkTheme(AppThemeMode.LIGHT, systemDarkTheme = true))
        assertTrue(resolveUseDarkTheme(AppThemeMode.FOLLOW_SYSTEM, systemDarkTheme = true))
        assertFalse(resolveUseDarkTheme(AppThemeMode.FOLLOW_SYSTEM, systemDarkTheme = false))
    }
}
