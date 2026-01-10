package hikki.sdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Proxy
import android.util.ArrayMap
import android.util.Log
import hikki.sdk.hooks.HikkiSdk
import java.net.Authenticator
import java.net.PasswordAuthentication

class ProxyUtils {
    enum class ProxyType {
        HTTP,
        HTTPS,
        SOCKS
    }

    companion object {
        private const val HTTP_PROXY_HOST = "http.proxyHost"
        private const val HTTP_PROXY_PORT = "http.proxyPort"
        private const val HTTP_PROXY_USER = "http.proxyUser"
        private const val HTTP_PROXY_PASSWORD = "http.proxyPassword"
        private const val HTTPS_PROXY_HOST = "https.proxyHost"
        private const val HTTPS_PROXY_PORT = "https.proxyPort"
        private const val HTTPS_PROXY_USER = "https.proxyUser"
        private const val HTTPS_PROXY_PASSWORD = "https.proxyPassword"
        private const val SOCKS_PROXY_HOST = "socksProxyHost"
        private const val SOCKS_PROXY_PORT = "socksProxyPort"

        /**
         * Sets a proxy for the given type.
         * To save the proxy settings, you would typically persist the host, port, and type
         * in SharedPreferences and re-apply them when the app starts.
         */
        @JvmStatic
        fun setProxy(host: String, port: Int, type: ProxyType, user: String? = null, pass: String? = null) {
            resetAllProxies() // drop all other proxy to apply new proxy

            val portStr = port.toString()
            when (type) {
                ProxyType.HTTP -> {
                    System.setProperty(HTTP_PROXY_HOST, host)
                    System.setProperty(HTTP_PROXY_PORT, portStr)
                    if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                        System.setProperty(HTTP_PROXY_USER, user)
                        System.setProperty(HTTP_PROXY_PASSWORD, pass)
                        Authenticator.setDefault(object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(user, pass.toCharArray())
                            }
                        })
                    } else {
                        System.clearProperty(HTTP_PROXY_USER)
                        System.clearProperty(HTTP_PROXY_PASSWORD)
                        Authenticator.setDefault(null)
                    }
                }

                ProxyType.HTTPS -> {
                    System.setProperty(HTTPS_PROXY_HOST, host)
                    System.setProperty(HTTPS_PROXY_PORT, portStr)
                    if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                        System.setProperty(HTTPS_PROXY_USER, user)
                        System.setProperty(HTTPS_PROXY_PASSWORD, pass)
                        Authenticator.setDefault(object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(user, pass.toCharArray())
                            }
                        })
                    } else {
                        System.clearProperty(HTTPS_PROXY_USER)
                        System.clearProperty(HTTPS_PROXY_PASSWORD)
                        Authenticator.setDefault(null)
                    }
                }

                ProxyType.SOCKS -> {
                    System.setProperty(SOCKS_PROXY_HOST, host)
                    System.setProperty(SOCKS_PROXY_PORT, portStr)
                    if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                        Authenticator.setDefault(object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(user, pass.toCharArray())
                            }
                        })
                    } else {
                        Authenticator.setDefault(null)
                    }
                }
            }
        }

        /**
         * Resets the proxy for the given type.
         */
        @JvmStatic
        fun resetProxy(type: ProxyType) {
            when (type) {
                ProxyType.HTTP -> {
                    System.clearProperty(HTTP_PROXY_HOST)
                    System.clearProperty(HTTP_PROXY_PORT)
                    System.clearProperty(HTTP_PROXY_USER)
                    System.clearProperty(HTTP_PROXY_PASSWORD)
                }

                ProxyType.HTTPS -> {
                    System.clearProperty(HTTPS_PROXY_HOST)
                    System.clearProperty(HTTPS_PROXY_PORT)
                    System.clearProperty(HTTPS_PROXY_USER)
                    System.clearProperty(HTTPS_PROXY_PASSWORD)
                }

                ProxyType.SOCKS -> {
                    System.clearProperty(SOCKS_PROXY_HOST)
                    System.clearProperty(SOCKS_PROXY_PORT)
                }
            }
            Authenticator.setDefault(null)
        }

        /**
         * Resets all proxies (HTTP, HTTPS, SOCKS).
         */
        @JvmStatic
        fun resetAllProxies() {
            resetProxy(ProxyType.HTTP)
            resetProxy(ProxyType.HTTPS)
            resetProxy(ProxyType.SOCKS)
        }
    }

    fun forceProxyApplying() {
        Log.d("ProxyUtils", "Setting proxy...")
        try {
            val context = HikkiSdk.context
            val applicationClass = Class.forName("android.app.Application")

            @SuppressLint("DiscouragedPrivateApi")
            val mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk")
            mLoadedApkField.isAccessible = true
            val mLoadedApkObject = mLoadedApkField.get(context)

            @SuppressLint("PrivateApi")
            val loadedApkClass = Class.forName("android.app.LoadedApk")

            @SuppressLint("DiscouragedPrivateApi")
            val mReceiversField = loadedApkClass.getDeclaredField("mReceivers")
            mReceiversField.isAccessible = true

            // Safe cast to ArrayMap
            val receivers = mReceiversField.get(mLoadedApkObject) as? ArrayMap<*, *>

            receivers?.values?.forEach { receiverMap ->
                // receiverMap is also an ArrayMap in the internal Android structure
                (receiverMap as? ArrayMap<*, *>)?.keys?.forEach { receiver ->
                    val receiverClass = receiver.javaClass

                    if (receiverClass.name.contains("ProxyChangeListener")) {
                        val onReceiveMethod =
                            receiverClass.getDeclaredMethod("onReceive", Context::class.java, Intent::class.java)
                        val intent = Intent(Proxy.PROXY_CHANGE_ACTION)
                        onReceiveMethod.invoke(receiver, context, intent)
                    } else {
                        // Check fields if the class name doesn't match directly
                        for (field in receiverClass.declaredFields) {
                            if (field.type.name.contains("ProxyChangeListener")) {
                                val onReceiveMethod = receiverClass.getDeclaredMethod(
                                    "onReceive",
                                    Context::class.java,
                                    Intent::class.java
                                )
                                val intent = Intent(Proxy.PROXY_CHANGE_ACTION)
                                onReceiveMethod.invoke(receiver, context, intent)
                                break
                            }
                        }
                    }
                }
            }

            Log.d("ProxyUtils", "Setting proxy successful!")
        } catch (e: Exception) {
            Log.d("ProxyUtils", "Setting proxy failed!")
            e.printStackTrace() // Good practice to print the stack trace for debugging
        }
    }
}