package kr.co.iboss.chat.FCM


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.*
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.SplashScreenActivity
import kr.co.iboss.chat.Utils.PreferencesUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference


class MyFirebaseMessagingService : SendBirdPushHandler() {

    companion object {
        private val FCM_TAG = "MyFirebaseMsgService"
    }

    interface ITokenResult {
        fun onPushTokenReceived(pushToken: String, e: SendBirdException?)
    }

    private val mPushToken = AtomicReference<String>()

    override fun onMessageReceived(context: Context?, remoteMessage: RemoteMessage?) {
        Log.d(FCM_TAG, "From: " + remoteMessage!!.from)

        if (remoteMessage!!.data.isNotEmpty()) {
            Log.d(FCM_TAG, "Message data payload: " + remoteMessage!!.data)
        }


        if (remoteMessage!!.notification != null) {
            Log.d(FCM_TAG, "Message Notification Body: " + remoteMessage!!.notification!!.body)
        }

        var channelUrl: String? = null
        try {
            if (remoteMessage!!.data.containsKey("sendbird")) {
                val sendBird = JSONObject(remoteMessage!!.data["sendbird"])
                val channel = sendBird["channel"] as JSONObject
                channelUrl = channel["channel_url"] as String
                SendBird.markAsDelivered(channelUrl)

                sendNotification(context, remoteMessage!!.data["message"], channelUrl)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun isUniquePushToken(): Boolean {
        return false
    }

    override fun onNewToken(newToken: String?) {
        Log.e(FCM_TAG, "onNewToken($newToken)")
        mPushToken.set(newToken)
    }

    private fun sendNotification(context: Context?, messageBody: String?, channelUrl : String) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_ID = "CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= 26) {
            val mChannel =
                NotificationChannel(CHANNEL_ID, "아이보스 채팅 푸시", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }

        val intent = Intent(context, SplashScreenActivity::class.java)
        intent.putExtra("groupChannelUrl", channelUrl)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.img_notification)
            .setColor(Color.parseColor("#7469C4"))
            .setLargeIcon(BitmapFactory.decodeResource(context!!.resources, R.drawable.app_logo))
            .setContentTitle(context!!.resources.getString(R.string.app_name))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        if (PreferencesUtils(context!!).getNotificationsShowPreviews()) {
            notificationBuilder.setContentText(messageBody)
        } else {
            notificationBuilder.setContentText("메시지가 도착했습니다.")
        }

        notificationManager.notify(0 , notificationBuilder.build())
    }

    private fun getPushToken(listener : ITokenResult) {
        var token = mPushToken.get()
        if (!TextUtils.isEmpty(token)) {
            listener.onPushTokenReceived(token, null)
            return
        }

        SendBirdPushHelper.getPushToken { token1: String, e: SendBirdException? ->
            Log.d(FCM_TAG, "FCM token : $token1")
            if (listener != null) {
                listener.onPushTokenReceived(token1, e)
            }
            if (e == null) {
                mPushToken.set(token1)
            }
        }
    }
}