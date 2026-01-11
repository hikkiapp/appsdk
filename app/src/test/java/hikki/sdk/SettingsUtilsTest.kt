package hikki.sdk

import hikki.sdk.manager.SettingsManager
import hikki.sdk.utils.ProxyUtils
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class SettingsUtilsTest {
    @Test
    fun `localized returns correct string for RU locale`() {
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("ru"))
        try {
            val result = "English".localized(ru = "Русский")
            assertEquals("Русский", result)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `localized returns correct string for BE locale`() {
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("be"))
        try {
            val result = "English".localized(be = "Беларуская")
            assertEquals("Беларуская", result)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `localized returns default string for unknown locale`() {
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
        try {
            val result = "English".localized(ru = "Русский")
            assertEquals("English", result)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `getDefaultSettings returns expected items`() {
        val settings = getDefaultSettings()
        assertTrue(settings.any { it.name == "general" && it is ConfigItem.SectionItem })
        assertTrue(settings.any { it.name == "enable_subscription" && it is ConfigItem.BooleanItem })
        assertTrue(settings.any { it.name == "proxy_type" && it is ConfigItem.EnumItem })
    }

    @Test
    fun `processButtonItem apply_proxy calls expected methods`() {
        val settingsManager = mockk<SettingsManager>()

        mockkObject(ProxyUtils.Companion)
        mockkConstructor(ProxyUtils::class)

        val settings = listOf(
            ConfigItem.EnumItem("Type", "proxy_type", "proxy", null, listOf("HTTP"), "HTTP"),
            ConfigItem.StringItem("Host", "proxy_host", "proxy", null, "127.0.0.1"),
            ConfigItem.IntItem("Port", "proxy_port", "proxy", null, 8888),
            ConfigItem.StringItem("User", "proxy_user", "proxy", null, "user"),
            ConfigItem.StringItem("Pass", "proxy_password", "proxy", null, "pass")
        )

        every { settingsManager.getSettings() } returns settings
        every { settingsManager.saveProxySettings(any(), any(), any(), any(), any()) } just Runs
        every { ProxyUtils.setProxy(any(), any(), any(), any(), any()) } just Runs
        every { anyConstructed<ProxyUtils>().forceProxyApplying() } just Runs

        val item = ConfigItem.ButtonItem("Apply", "apply_proxy", "proxy", null)
        processButtonItem(settingsManager, item)

        verify { settingsManager.saveProxySettings("HTTP", "127.0.0.1", 8888, "user", "pass") }
        verify { ProxyUtils.setProxy("127.0.0.1", 8888, ProxyUtils.ProxyType.HTTP, "user", "pass") }
        verify { anyConstructed<ProxyUtils>().forceProxyApplying() }

        unmockkAll()
    }

    @Test
    fun `processButtonItem reset_proxy calls expected methods`() {
        val settingsManager = mockk<SettingsManager>()

        mockkObject(ProxyUtils.Companion)
        mockkConstructor(ProxyUtils::class)

        every { settingsManager.clearProxySettings() } just Runs
        every { ProxyUtils.resetAllProxies() } just Runs
        every { anyConstructed<ProxyUtils>().forceProxyApplying() } just Runs

        val item = ConfigItem.ButtonItem("Reset", "reset_proxy", "proxy", null)
        processButtonItem(settingsManager, item)

        verify { settingsManager.clearProxySettings() }
        verify { ProxyUtils.resetAllProxies() }
        verify { anyConstructed<ProxyUtils>().forceProxyApplying() }

        unmockkAll()
    }
}