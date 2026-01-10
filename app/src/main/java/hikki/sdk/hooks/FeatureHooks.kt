package hikki.sdk.hooks

class FeatureHooks {
    private val featureSettings = HikkiSdk.featureSettings

    fun isSubscribed(): Boolean {
        return featureSettings.isFeatureEnabled("enable_subscription", "general", false)
    }

    fun getSubscribedExpireDate(): Long {
        return featureSettings.getDateSetting("subscription_expire_date", "general", -1L)
    }
}