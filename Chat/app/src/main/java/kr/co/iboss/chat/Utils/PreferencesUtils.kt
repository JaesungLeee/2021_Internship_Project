package kr.co.iboss.chat.Utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesUtils(context: Context) {

    companion object {
        private val PREFERENCES_KEY_USER_ID             = "PREFERENCES_KEY_USER_ID"
        private val PREFERENCES_KEY_USER_NICK_NAME      = "PREFERENCES_KEY_USER_NICK_NAME"
        private val PREFERENCES_KEY_USER_PROFILE_IMAGE  = "PREFERENCES_KEY_USER_PROFILE_IMAGE"

        private val PREFERENCE_KEY_NOTIFICATIONS = "notifications"
        private val PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews"
        private val PREFERENCE_KEY_CONNECTED = "connected"
    }


    private val preferences : SharedPreferences = context.getSharedPreferences("sendbird", Context.MODE_PRIVATE)


    fun setConnected(tf : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply()
    }

    fun getConnected() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_CONNECTED, false)
    }

    fun setUserId(userId : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_ID, userId).apply()
    }

    fun getUserId() : String {
        return preferences.getString(PREFERENCES_KEY_USER_ID, "").toString()
    }

    fun setUserNickName(userNickName : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_NICK_NAME, userNickName).apply()
    }

    fun getUserNickName() : String {
        return preferences.getString(PREFERENCES_KEY_USER_NICK_NAME, "").toString()
    }

    fun setUserProfileImg(userProfileImage : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_PROFILE_IMAGE, userProfileImage).apply()
    }

    fun getUserProfileImage() : String {
        return preferences.getString(PREFERENCES_KEY_USER_PROFILE_IMAGE, "").toString()
    }

    fun setNotification(notification : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notification).apply()
    }

    fun getNotification() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsShowPreviews(notificationShowPreview : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationShowPreview).apply()
    }

    fun getNotificationsShowPreviews() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true)
    }






}