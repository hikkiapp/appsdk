package hikki.sdk.ghost

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean


object GhostFramework {
    private const val TAG = "GhostFramework"

    private val isInitialized = AtomicBoolean(false)
    private lateinit var config: GhostConfig
    private lateinit var cachedSignatures: Array<Signature>

    fun attach(context: Context, configuration: GhostConfig) {
        Log.d(TAG, "attach() called.")
        if (isInitialized.getAndSet(true)) {
            Log.w(TAG, "GhostFramework is already initialized.")
            return
        } else if (configuration.base64Signatures.isEmpty()) {
            Log.e(TAG, "Signatures is empty.")
            return
        }

        this.config = configuration

        val start = System.currentTimeMillis()
        try {
            Log.d(TAG, "Initializing GhostFramework...")
            parseSignatures(config.base64Signatures)
            unseal()
            injectProxy(context)
            Log.i(TAG, "GhostFramework initialized in ${System.currentTimeMillis() - start}ms")
        } catch (e: Throwable) {
            Log.e(TAG, "Critical Failure initializing GhostFramework", e)
        }
    }

    fun unseal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return
        }

        try {
            val success = HiddenApiBypass.addHiddenApiExemptions("L")

            if (success) {
                Log.i(TAG, "Hidden API bypass successful (LSPosed).")
            } else {
                Log.w(TAG, "Hidden API bypass returned false. Check logs.")
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Hidden API bypass failed completely.", e)
        }
    }

    private fun parseSignatures(base64: String) {
        Log.d(TAG, "parseSignatures() called.")
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                val count = input.read() and 0xFF
                val list = ArrayList<Signature>(count)
                repeat(count) {
                    val len = input.readInt()
                    val rawSig = ByteArray(len)
                    input.readFully(rawSig)
                    list.add(Signature(rawSig))
                }
                cachedSignatures = list.toTypedArray()
                Log.d(TAG, "Successfully parsed ${cachedSignatures.size} signatures.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse signatures.", e)
            throw IllegalArgumentException("Invalid Signature Data", e)
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun injectProxy(context: Context) {
        Log.d(TAG, "injectProxy() called.")
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val activityThread = currentActivityThreadMethod.invoke(null)
            ?: throw IllegalStateException("Failed to get ActivityThread")
        Log.d(TAG, "Got ActivityThread: $activityThread")

        val sPackageManagerField = ReflectionHelper.findField(activityThreadClass, "sPackageManager")
        val originalIPm = sPackageManagerField.get(activityThread)
            ?: throw IllegalStateException("sPackageManager is null")
        Log.d(TAG, "Got original sPackageManager.")

        val iPackageManagerClass = Class.forName("android.content.pm.IPackageManager")
        val handler = PmsInvocationHandler(originalIPm, context.packageName, config, cachedSignatures)

        val proxy = Proxy.newProxyInstance(
            iPackageManagerClass.classLoader,
            arrayOf(iPackageManagerClass),
            handler
        )
        Log.d(TAG, "Created IPackageManager proxy.")

        sPackageManagerField.set(activityThread, proxy)
        Log.d(TAG, "Injected proxy into ActivityThread.sPackageManager.")

        val pm = context.packageManager
        try {
            val mPmField = ReflectionHelper.findField(pm.javaClass, "mPM")
            mPmField.set(pm, proxy)
            Log.d(TAG, "Injected proxy into ApplicationPackageManager.mPM.")
        } catch (e: Exception) {
            Log.w(TAG, "Could not replace local mPM (Global hook still active)", e)
        }
    }
}