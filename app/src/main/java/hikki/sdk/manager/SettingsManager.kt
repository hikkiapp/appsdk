package hikki.sdk.manager

import android.content.Context
import android.content.SharedPreferences
import hikki.sdk.CURRENT_VERSION
import hikki.sdk.ConfigItem

import hikki.sdk.getDefaultSettings
import hikki.sdk.utils.ProxyUtils
import kotlinx.serialization.json.Json

class SettingsManager(context: Context) {
    private val prefsName: String = "hikki_sdk_settings"
    private val versionKey: String = "version"
    private val proxyPrefsName: String = "hikki_sdk_proxy_settings"

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    internal val proxyPrefs: SharedPreferences = context.getSharedPreferences(proxyPrefsName, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val ConfigItem.storageKey: String
        get() = if (this is ConfigItem.SectionItem) {
            "section_$name"
        } else {
            "setting_${section}_$name"
        }

    fun getSettings(): List<ConfigItem> {
        val storedVersion = prefs.getInt(versionKey, 0)
        if (storedVersion != CURRENT_VERSION) {
            val editor = prefs.edit()
            editor.clear()
            editor.putInt(versionKey, CURRENT_VERSION)
            editor.apply()
            val defaults = getDefaultSettings()
            saveSettings(defaults)
            return defaults
        }

        val defaults = getDefaultSettings()

        return defaults.map { defaultItem ->
            val savedJson = prefs.getString(defaultItem.storageKey, null)

            if (savedJson != null) {
                try {
                    json.decodeFromString<ConfigItem>(savedJson)
                } catch (e: Exception) {
                    defaultItem
                }
            } else {
                defaultItem
            }
        }
    }

    fun saveSettings(items: List<ConfigItem>) {
        val editor = prefs.edit()
        editor.putInt(versionKey, CURRENT_VERSION)
        items.forEach { item ->
            val itemJson = json.encodeToString(item)
            editor.putString(item.storageKey, itemJson)
        }

        editor.apply()
    }

    fun applyProxyOnStart() {
        val proxyType = proxyPrefs.getString("proxy_type", null)
        if (proxyType != null) {
            val host = proxyPrefs.getString("proxy_host", "") ?: ""
            val port = proxyPrefs.getInt("proxy_port", 8080)
            val user = proxyPrefs.getString("proxy_user", null)
            val pass = proxyPrefs.getString("proxy_password", null)
            val type = ProxyUtils.ProxyType.valueOf(proxyType)
            ProxyUtils.setProxy(host, port, type, user, pass)
            ProxyUtils().forceProxyApplying()
        } else {
            // Prefs are empty, check if system properties for proxy are set
            if (!System.getProperty("http.proxyHost").isNullOrEmpty() ||
                !System.getProperty("https.proxyHost").isNullOrEmpty() ||
                !System.getProperty("socksProxyHost").isNullOrEmpty()
            ) {
                ProxyUtils.resetAllProxies()
                ProxyUtils().forceProxyApplying()
            }
        }
    }

    internal fun saveProxySettings(proxyType: String, host: String, port: Int, user: String?, pass: String?) {
        val editor = proxyPrefs.edit()
        editor.putString("proxy_type", proxyType)
        editor.putString("proxy_host", host)
        editor.putInt("proxy_port", port)
        editor.putString("proxy_user", user)
        editor.putString("proxy_password", pass)
        editor.apply()
    }

    internal fun clearProxySettings() {
        proxyPrefs.edit().clear().apply()
    }
}