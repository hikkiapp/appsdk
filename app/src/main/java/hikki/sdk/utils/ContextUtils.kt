package hikki.sdk.utils

import android.annotation.SuppressLint
import android.content.Context

object ContextUtils {
    /**
     * Attempts to retrieve the application context via reflection.
     * This is a fallback mechanism and might not work on all Android versions or devices.
     *
     * @return The application context if successful, null otherwise.
     */
    @SuppressLint("PrivateApi")
    fun getApplicationContextViaReflection(): Context? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null)
            val getApplicationMethod = activityThreadClass.getMethod("getApplication")
            getApplicationMethod.invoke(currentActivityThread) as? Context
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}