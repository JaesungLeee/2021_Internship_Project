package kr.co.iboss.ibosschat

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtils {

    private val PREFERENCES_KEY_USER_ID                     = "PREFERENCES_KEY_USER_ID"
    private val PREFERENCES_KEY_USER_NICK_NAME              = "PREFERENCES_KEY_USER_NICK_NAME"
    private val PREFERENCE_KEY_CONNECTED                    = "PREFERENCE_KEY_CONNECTED"
    private val PREFERENCE_KEY_NOTIFICATIONS                = "PREFERENCE_KEY_NOTIFICATIONS"
    private val PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS  = "PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS"


    private var mContext : Context? = null

    private val preference : SharedPreferences
        get() = mContext!!.getSharedPreferences("sendbird", Context.MODE_PRIVATE)

    var isConnected : Boolean
        get() = preference.getBoolean(PREFERENCE_KEY_CONNECTED, false)
        set(tf) {
            val editor = preference.edit()
            editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply()
        }

    var userId : String
        get() = preference.getString(PREFERENCES_KEY_USER_ID, "").toString()
        set(userId) {
            val editor = preference.edit()
            editor.putString(PREFERENCES_KEY_USER_ID, userId).apply()
        }

    var userNickName : String
        get() = preference.getString(PREFERENCES_KEY_USER_NICK_NAME, "").toString()
        set(userNickName) {
            val editor = preference.edit()
            editor.putString(PREFERENCES_KEY_USER_NICK_NAME, userNickName).apply()
        }

    var notificationShowPreviews : Boolean
        get() = preference.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true)
        set(notificationShowPreviews) {
            val editor = preference.edit()
            editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationShowPreviews)
        }


}