package hikki.sdk.ghost

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

internal class PmsInvocationHandler(
    private val base: Any,
    val appPkgName: String,
    private val config: GhostConfig,
    private val cachedSignatures: Array<Signature>
) : InvocationHandler {
    private val TAG = "GhostFramework"
    private val GET_SIGNATURES = 64
    private val GET_SIGNING_CERTIFICATES = 134217728

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        val name = method.name
        Log.d(TAG, "PmsInvocationHandler.invoke: $name")

        try {
            when (name) {
                "getPackageInfo" -> return handleGetPackageInfo(method, args)
                "getInstallerPackageName" -> return handleGetInstaller(method, args)
                "getApplicationInfo" -> return handleGetAppInfo(method, args)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hook logic error in $name", e)
        }

        return try {
            method.invoke(base, *(args ?: emptyArray()))
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking original method $name", e)
            throw e.cause ?: e
        }
    }

    @Suppress("DEPRECATION")
    private fun handleGetPackageInfo(method: Method, args: Array<out Any>?): Any? {
        val pkgName = args?.getOrNull(0) as? String
        val flags = args?.getOrNull(1) as? Int ?: 0
        Log.d(TAG, "handleGetPackageInfo for pkg=$pkgName, flags=$flags")

        val result = method.invoke(base, *(args ?: emptyArray())) as? PackageInfo ?: return null

        if (pkgName == appPkgName) {
            Log.d(TAG, "Spoofing package info for $pkgName")

            config.spoofVersionCode?.let { versionCode ->
                Log.d(TAG, "Spoofing versionCode to $versionCode")
                result.versionCode = versionCode.toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    result.longVersionCode = versionCode
                }
            }

            config.spoofVersionName?.let {
                Log.d(TAG, "Spoofing versionName to $it")
                result.versionName = it
            }

            if ((flags and GET_SIGNATURES) != 0) {
                Log.d(TAG, "Spoofing signatures (legacy).")
                result.signatures = cachedSignatures
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if ((flags and GET_SIGNING_CERTIFICATES) != 0) {
                    Log.d(TAG, "Spoofing signing certificates (modern).")
                    if (result.signatures != null) {
                        result.signatures = cachedSignatures
                    }
                    spoofSigningInfoSafe(result)
                }
            }

            if (config.spoofDebugFlag) {
                Log.d(TAG, "Cleaning debug flags.")
                cleanDebugFlags(result)
            }
        }

        return result
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun spoofSigningInfoSafe(info: PackageInfo) {
        Log.d(TAG, "spoofSigningInfoSafe() called.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return

            try {
                val signingInfoClass = signingInfo.javaClass
                val mSigningDetailsField = try {
                    signingInfoClass.getDeclaredField("mSigningDetails")
                } catch (e: NoSuchFieldException) {
                    null
                }

                mSigningDetailsField?.isAccessible = true
                val signingDetails = mSigningDetailsField?.get(signingInfo) ?: return

                var currentClass: Class<*>? = signingDetails.javaClass
                var replaced = false

                while (currentClass != null && currentClass != Any::class.java) {
                    for (field in currentClass.declaredFields) {
                        if (field.type.isArray && field.type.componentType == Signature::class.java) {
                            field.isAccessible = true
                            field.set(signingDetails, cachedSignatures)
                            replaced = true
                            Log.d(TAG, "Found and replaced Signature[] in ${currentClass.name}")
                        }
                    }
                    currentClass = currentClass.superclass
                }

                if (!replaced) {
                    Log.w(TAG, "Could not find Signature[] array inside SigningDetails")
                } else {
                    Log.d(TAG, "Successfully spoofed SigningInfo.")
                }

            } catch (e: Exception) {
                Log.w(TAG, "Failed to spoof SigningInfo internals on SDK ${Build.VERSION.SDK_INT}", e)
                info.signingInfo = null
            }
        }
    }

    private fun cleanDebugFlags(info: PackageInfo) {
        val appInfo = info.applicationInfo ?: return
        Log.d(TAG, "cleanDebugFlags() called. Original flags: ${appInfo.flags}")
        appInfo.flags = appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        Log.d(TAG, "Cleaned flags: ${appInfo.flags}")
    }

    private fun handleGetInstaller(method: Method, args: Array<out Any>?): Any? {
        val pkgName = args?.getOrNull(0) as? String
        if (config.spoofInstaller && pkgName == appPkgName) {
            Log.d(TAG, "Spoofing installer for $pkgName to ${config.fakeInstallerName}")
            return config.fakeInstallerName
        }
        return method.invoke(base, *(args ?: emptyArray()))
    }

    private fun handleGetAppInfo(method: Method, args: Array<out Any>?): Any? {
        val result = method.invoke(base, *(args ?: emptyArray())) as? ApplicationInfo ?: return null
        val pkgName = args?.getOrNull(0) as? String

        if (config.spoofDebugFlag && pkgName == appPkgName) {
            Log.d(TAG, "Cleaning debug flags in getApplicationInfo for $pkgName")
            result.flags = result.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        }
        return result
    }
}