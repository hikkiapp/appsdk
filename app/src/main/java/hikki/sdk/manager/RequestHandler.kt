package hikki.sdk.manager

import android.content.Context
import android.content.Intent
import android.util.Log
import hikki.sdk.*
import hikki.sdk.hooks.HikkiSdk
import hikki.sdk.utils.Encryptor
import kotlinx.serialization.json.Json

internal object RequestHandler {
    fun handle(context: Context, intent: Intent) {
        HikkiSdk.init(context)

        val settingsManager = HikkiSdk.settingsManager
        val jsonFormat = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

        when (intent.action) {
            ConfigActions.GET -> {
                val settings = settingsManager.getSettings()
                val payload = ConfigPayload(CURRENT_VERSION, settings)
                val jsonPayload = jsonFormat.encodeToString(ConfigPayload.serializer(), payload)
                val morsePayload = Encryptor.encodeToBase64(jsonPayload)

                Log.d("RequestHandler", "Returning settings: $jsonPayload")

                val responseIntent = Intent(ConfigActions.GET).apply {
                    putExtra("json_payload", morsePayload)
                    `package` = intent.getStringExtra("sender_package")
                }

                context.sendBroadcast(responseIntent)
            }
            ConfigActions.SET -> {
                val morsePayload = intent.getStringExtra("json_payload")
                if (morsePayload != null) {
                    try {
                        val jsonPayload = Encryptor.decodeFromBase64(morsePayload)
                        when (intent.getStringExtra("action")) {
                            "button_click" -> {
                                val item = jsonFormat.decodeFromString(ConfigItem.serializer(), jsonPayload)
                                val expectedHash = calculateConfigItemHash(item.name, item.section)
                                if (item.hash == expectedHash) {
                                    if (item is ConfigItem.ButtonItem) {
                                        processButtonItem(settingsManager, item)
                                    }
                                } else {
                                    Log.w(
                                        "RequestHandler",
                                        "Hash mismatch for button click '${item.name}'. Discarding."
                                    )
                                }
                            }

                            else -> {
                                val payload = jsonFormat.decodeFromString(ConfigPayload.serializer(), jsonPayload)

                                if (payload.version == CURRENT_VERSION) {
                                    val verifiedSettings = payload.settings.filter { item ->
                                        val expectedHash = calculateConfigItemHash(item.name, item.section)
                                        if (item.hash == expectedHash) {
                                            true
                                        } else {
                                            Log.w(
                                                "RequestHandler",
                                                "Hash mismatch for item '${item.name}'. Discarding."
                                            )
                                            false
                                        }
                                    }
                                    Log.d("RequestHandler", "Saving settings: $verifiedSettings")
                                    settingsManager.saveSettings(verifiedSettings)
                                } else {
                                    Log.w("RequestHandler", "Version mismatch. Ignoring settings update.")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}