package hikki.sdk.hooks

class FeatureHooks {
    private val featureSettings = HikkiSdk.featureSettings

    fun isSubscribed(): Boolean {
        return featureSettings.isFeatureEnabled("enable_subscription", "general", false)
    }

    fun getSubscribedExpireDate(): Long {
        return featureSettings.getDateSetting("subscription_expire_date", "general", -1L)
    }

    /**
     * Generic methods to access settings directly.
     * These can be used for custom features not explicitly defined in FeatureHooks.
     */

    fun isFeatureEnabled(prefName: String, section: String, defaultValue: Boolean = false): Boolean {
        return featureSettings.isFeatureEnabled(prefName, section, defaultValue)
    }

    fun getStringSetting(prefName: String, section: String, defaultValue: String): String {
        return featureSettings.getStringSetting(prefName, section, defaultValue)
    }

    fun getIntSetting(prefName: String, section: String, defaultValue: Int): Int {
        return featureSettings.getIntSetting(prefName, section, defaultValue)
    }

    fun getFloatSetting(prefName: String, section: String, defaultValue: Float): Float {
        return featureSettings.getFloatSetting(prefName, section, defaultValue)
    }

    fun getEnumSetting(prefName: String, section: String, defaultValue: String): String {
        return featureSettings.getEnumSetting(prefName, section, defaultValue)
    }

    fun getDateSetting(prefName: String, section: String, defaultValue: Long): Long {
        return featureSettings.getDateSetting(prefName, section, defaultValue)
    }

    fun getTimeSetting(prefName: String, section: String, defaultValue: Long): Long {
        return featureSettings.getTimeSetting(prefName, section, defaultValue)
    }
}