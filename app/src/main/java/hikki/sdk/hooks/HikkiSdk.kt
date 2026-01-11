package hikki.sdk.hooks

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import hikki.sdk.ghost.GhostConfig
import hikki.sdk.ghost.GhostFramework
import hikki.sdk.manager.FeatureSettings
import hikki.sdk.manager.SettingsManager
import hikki.sdk.receiver.RemoteSettingsReceiver
import hikki.sdk.utils.ContextUtils

/**
 * Main entry point for the Hikki SDK.
 * This object manages the initialization and provides access to core SDK components.
 */
@SuppressLint("StaticFieldLeak")
object HikkiSdk {
    @Volatile
    private var internalContext: Context? = null

    /**
     * Provides the application context.
     * If the SDK hasn't been initialized, it attempts to retrieve the context via reflection.
     * @throws IllegalStateException if initialization fails and reflection cannot retrieve the context.
     */
    val context: Context
        get() {
            return internalContext ?: synchronized(this) {
                internalContext ?: run {
                    val appContext = ContextUtils.getApplicationContextViaReflection()
                    if (appContext != null) {
                        init(appContext)
                        internalContext!!
                    } else {
                        throw IllegalStateException("HikkiSdk has not been initialized and failed to get context via reflection. Please call HikkiSdk.init(context) in your Application class.")
                    }
                }
            }
        }

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var featureHooks: FeatureHooks
        private set

    lateinit var featureSettings: FeatureSettings
        private set

    /**
     * Initializes the Ghost framework with default configurations.
     * Make init from base attachments inside MainActivity
     * @param context The context used for initialization.
     */
    fun initGhost(context: Context) {
        val base64Signatures = ""

        val ghostConfig = GhostConfig(
            base64Signatures = base64Signatures,
            spoofInstaller = true,
            spoofDebugFlag = true,
        )

        Log.d("HikkiSdk", "Ghost initialized with config: $ghostConfig")

        GhostFramework.attach(context, ghostConfig)
    }

    /**
     * Initializes the Hikki SDK using the application instance.
     * Initialize it inside Application class.
     * @param application The application instance.
     */
    fun init(application: Application) {
        init(application.applicationContext)
    }

    /**
     * Initializes the Hikki SDK using a context in MainActivity/Application class.
     * This method ensures thread-safe, single-time initialization of core components.
     * @param context The context used for initialization.
     * @throws RuntimeException if RemoteSettingsReceiver is not properly declared in AndroidManifest.xml.
     */
    @Suppress("D")
    fun init(context: Context) {
        // Double-checked locking to ensure thread safety and single initialization
        if (internalContext == null) {
            synchronized(this) {
                if (internalContext == null) {
                    val appContext = context.applicationContext

                    internalContext = appContext
                    settingsManager = SettingsManager(appContext)
                    featureSettings = FeatureSettings()
                    featureHooks = FeatureHooks()

                    val receiver = ComponentName(appContext, RemoteSettingsReceiver::class.java)
                    val packageManager = appContext.packageManager
                    val status = packageManager.getComponentEnabledSetting(receiver)
                    if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                        status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                        throw RuntimeException("RemoteSettingsReceiver is not enabled! Please check your AndroidManifest.xml")
                    }

                    settingsManager.applyProxyOnStart()
                }
            }
        }
    }
}