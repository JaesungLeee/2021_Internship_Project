package kr.co.iboss.ibosschat

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
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.SendBirdException
import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.SendBirdPushHelper
import kr.co.iboss.ibosschat.UI.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference

class WebFCMServices : SendBirdPushHandler() {
    companion object {
        private val FCM_TAG = "FCM_SERVICES"
    }

    private val mPushDeviceToken = AtomicReference<String>()

    interface ITokenListener {
        fun onPushTokenReceived(pushToken : String, e : SendBirdException?)

    }

    override fun onNewToken(newToken: String?) {
        Log.e(FCM_TAG, "onNewToken : $newToken")
        mPushDeviceToken.set(newToken)
    }

    override fun isUniquePushToken(): Boolean {
        return false
    }

    override fun onMessageReceived(context: Context?, remoteMessage: RemoteMessage?) {
        Log.e(FCM_TAG, "From : ${remoteMessage!!.from}")

        if (remoteMessage.data.isNotEmpty()) Log.e(FCM_TAG, "Data Payload : ${remoteMessage!!.data}")
        if (remoteMessage.notification != null) Log.e(FCM_TAG, "Notification : ${remoteMessage.notification!!.body}")

        var channelUrl : String?

        try {
            if (remoteMessage.data.containsKey("sendbird")) {
                val sbObject = JSONObject(remoteMessage.data["sendbird"])
                val channelObject = sbObject["channel"] as JSONObject
                channelUrl = channelObject["channel_url"] as String

                val msgBody = remoteMessage.data["message"]
                sendNotification(context, msgBody, channelUrl)
            }
            else {
                Log.e(FCM_TAG, "WRONG_JSON_OBJECT")
            }
        }
        catch (e : JSONException) {
            e.printStackTrace()
        }

    }

    private fun sendNotification(context: Context?, msgBody : String?, channelUrl: String) {

        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "CHANNEL_ID"

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "아이보스 하이브리드 푸시", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val bundle = Bundle()
        bundle.putString("groupChannelUrl", channelUrl)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtras(bundle)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.img_notification)
            .setColor(Color.parseColor("#7469C4"))
            .setLargeIcon(BitmapFactory.decodeResource(context!!.resources, R.drawable.app_logo))
            .setContentTitle(context!!.resources.getString(R.string.app_name))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        if (PreferenceUtils.notificationShowPreviews) {
            notificationBuilder.setContentText(msgBody)
        } else notificationBuilder.setContentText("메시지가 도착했습니다.")

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun getPushToken(listener : ITokenListener) {
        val token = mPushDeviceToken.get()

        if (!TextUtils.isEmpty(token)) {
            listener.onPushTokenReceived(token, null)
            return
        }

        SendBirdPushHelper.getPushToken { token1, e ->
            Log.e(FCM_TAG, "FCM TOKEN : $token1")
            if (listener != null) listener.onPushTokenReceived(token1, e)

            if (e != null) mPushDeviceToken.set(token1)
        }
    }
}