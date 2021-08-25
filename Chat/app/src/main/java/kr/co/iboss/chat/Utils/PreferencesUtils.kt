package kr.co.iboss.chat.Utils

import android.content.Context
import android.content.SharedPreferences

/* SharedPreferences를 사용하여 디바이스 내의 특정 File에 어플리케이션 기본 정보 저장하는 Preference Utils */
class PreferencesUtils(context: Context) {

    companion object {
        private val PREFERENCES_KEY_USER_ID             = "PREFERENCES_KEY_USER_ID"
        private val PREFERENCES_KEY_USER_NICK_NAME      = "PREFERENCES_KEY_USER_NICK_NAME"
        private val PREFERENCES_KEY_USER_PROFILE_IMAGE  = "PREFERENCES_KEY_USER_PROFILE_IMAGE"

        private val PREFERENCE_KEY_NOTIFICATIONS = "notifications"
        private val PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews"
        private val PREFERENCE_KEY_CONNECTED = "connected"
    }

    /* preference 변수 초기화 */
    private val preferences : SharedPreferences = context.getSharedPreferences("sendbird", Context.MODE_PRIVATE)

    /* 연결 설정 상태 저장 */
    fun setConnected(tf : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply()
    }

    /* 연결 설정 상태 반환 */
    fun getConnected() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_CONNECTED, false)
    }

    /* UserID 저장 */
    fun setUserId(userId : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_ID, userId).apply()
    }

    /* User ID 반환 */
    fun getUserId() : String {
        return preferences.getString(PREFERENCES_KEY_USER_ID, "").toString()
    }

    /* NickName 저장 */
    fun setUserNickName(userNickName : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_NICK_NAME, userNickName).apply()
    }

    /* NickName 반환 */
    fun getUserNickName() : String {
        return preferences.getString(PREFERENCES_KEY_USER_NICK_NAME, "").toString()
    }

    /* UserProfile URL 저장 */
    fun setUserProfileImg(userProfileImage : String) {
        val editor = preferences.edit()
        editor.putString(PREFERENCES_KEY_USER_PROFILE_IMAGE, userProfileImage).apply()
    }

    /* UserProfile URL 반환 */
    fun getUserProfileImage() : String {
        return preferences.getString(PREFERENCES_KEY_USER_PROFILE_IMAGE, "").toString()
    }

    /* Notification 상태 저장 */
    fun setNotification(notification : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notification).apply()
    }

    /* Notification 상태 반환 */
    fun getNotification() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true)
    }

    /* Notification 표시 상태 저장*/
    fun setNotificationsShowPreviews(notificationShowPreview : Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationShowPreview).apply()
    }

    /* Notification 표시 상태 반환 */
    fun getNotificationsShowPreviews() : Boolean {
        return preferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true)
    }






}