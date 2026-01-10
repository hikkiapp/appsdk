package hikki.sdk.manager

import hikki.sdk.ConfigItem
import hikki.sdk.hooks.HikkiSdk

class FeatureSettings {
    private val settingsManager = HikkiSdk.settingsManager
    private var settings: List<ConfigItem> = emptyList()

    init {
        refreshSettings()
    }

    fun refreshSettings() {
        settings = settingsManager.getSettings()
    }

    fun isFeatureEnabled(prefName: String, section: String, defaultValue: Boolean = false): Boolean {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.BooleanItem)?.value
            ?: defaultValue
    }

    fun getStringSetting(prefName: String, section: String, defaultValue: String): String {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.StringItem)?.value
            ?: defaultValue
    }

    fun getIntSetting(prefName: String, section: String, defaultValue: Int): Int {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.IntItem)?.value
            ?: defaultValue
    }

    fun getFloatSetting(prefName: String, section: String, defaultValue: Float): Float {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.FloatItem)?.value
            ?: defaultValue
    }

    fun getEnumSetting(prefName: String, section: String, defaultValue: String): String {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.EnumItem)?.value
            ?: defaultValue
    }

    fun getDateSetting(prefName: String, section: String, defaultValue: Long): Long {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.DateItem)?.value
            ?: defaultValue
    }

    fun getTimeSetting(prefName: String, section: String, defaultValue: Long): Long {
        return (settings.find { it.name == prefName && it.section == section } as? ConfigItem.TimeItem)?.value
            ?: defaultValue
    }
}