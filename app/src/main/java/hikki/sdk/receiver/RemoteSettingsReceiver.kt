package hikki.sdk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import hikki.sdk.manager.RequestHandler

class RemoteSettingsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RequestHandler.handle(context, intent)
    }
}