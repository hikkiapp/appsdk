package hikki.sdk.ghost

import android.annotation.SuppressLint
import android.os.Build
import android.os.IBinder
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Proxy

@SuppressLint("DiscouragedPrivateApi", "PrivateApi", "PrivateApi")
object BinderHookHelper {
    private const val TAG = "BinderHookHelper"
    private const val SERVICE_MANAGER = "android.os.ServiceManager"

    private const val SCAN_INTERVAL_MS = 1000L

    fun interface HookFactory {
        fun onService(name: String, binder: IBinder): IBinder
    }

    @Volatile
    private var activeCache: HookedCache? = null
    @Volatile
    private var isScanning = false

    fun install(factory: HookFactory) {
        if (isScanning) return // Already running

        Log.i(TAG, "Initializing ServiceManager Hook...")

        attemptHook(factory)

        startWatchdog(factory)
    }

    private fun startWatchdog(factory: HookFactory) {
        isScanning = true
        val thread = Thread {
            while (isScanning) {
                try {
                    Thread.sleep(SCAN_INTERVAL_MS)
                    ensureIntegrity(factory)
                } catch (e: InterruptedException) {
                    break
                } catch (t: Throwable) {
                    Log.e(TAG, "Watchdog error", t)
                }
            }
        }
        thread.name = "Ghost_Watchdog"
        thread.isDaemon = true
        thread.start()
    }

    private fun ensureIntegrity(factory: HookFactory) {
        try {
            val smClass = Class.forName(SERVICE_MANAGER)
            val cacheField = smClass.getDeclaredField("sCache")
            cacheField.isAccessible = true
            val currentCache = cacheField.get(null)

            // If cache was replaced by system, re-hook
            if (currentCache !== activeCache) {
                Log.w(TAG, "Hook detached by system! Re-installing...")
                attemptHook(factory)
            }
        } catch (t: Throwable) { /* ignore */
        }
    }

    @Synchronized
    private fun attemptHook(factory: HookFactory) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.addHiddenApiExemptions("L")
            }

            val smClass = Class.forName(SERVICE_MANAGER)
            val cacheField = smClass.getDeclaredField("sCache")
            cacheField.isAccessible = true

            @Suppress("UNCHECKED_CAST")
            val originalCache = cacheField.get(null) as MutableMap<String, IBinder>

            if (originalCache is HookedCache) {
                activeCache = originalCache
                return
            }

            val hookedCache = HookedCache(originalCache, factory)

            // Copy existing services carefully (avoid ArrayMap concurrency crash)
            synchronized(originalCache) {
                val keys = ArrayList<String>(originalCache.size)
                val it = originalCache.keys.iterator()
                while (it.hasNext()) keys.add(it.next())

                for (key in keys) {
                    val binder = originalCache[key]
                    if (binder != null && binder.isBinderAlive && !Proxy.isProxyClass(binder.javaClass)) {
                        val hooked = factory.onService(key, binder)
                        if (hooked !== binder) originalCache[key] = hooked
                    }
                }
            }

            cacheField.set(null, hookedCache)
            activeCache = hookedCache
            Log.i(TAG, "ServiceManager hook INSTALLED.")

        } catch (t: Throwable) {
            Log.e(TAG, "Critical: FAILED to hook ServiceManager", t)
        }
    }

    private class HookedCache(
        private val base: MutableMap<String, IBinder>,
        private val factory: HookFactory
    ) : MutableMap<String, IBinder> by base {

        override fun get(key: String): IBinder? {
            val binder = base[key] ?: return null
            if (!binder.isBinderAlive) {
                base.remove(key)
                return null
            }
            if (Proxy.isProxyClass(binder.javaClass)) return binder

            val hooked = factory.onService(key, binder)
            if (hooked !== binder) base[key] = hooked
            return hooked
        }

        override fun put(key: String, value: IBinder): IBinder? {
            if (Proxy.isProxyClass(value.javaClass)) return base.put(key, value)

            Log.i(TAG, "New service detected: $key -> Injecting Hook")
            val hooked = factory.onService(key, value)
            return base.put(key, hooked)
        }

        override fun putAll(from: Map<out String, IBinder>) {
            from.forEach { (k, v) -> put(k, v) }
        }
    }
}