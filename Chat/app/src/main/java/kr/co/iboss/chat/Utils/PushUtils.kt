package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.SendBirdPushHelper

object PushUtils {
    fun registerPushHandler(handler : SendBirdPushHandler) {
        SendBirdPushHelper.registerPushHandler(handler)
    }

    fun unregisterPushHandler(listener : SendBirdPushHelper.OnPushRequestCompleteListener) {
        SendBirdPushHelper.unregisterPushHandler(listener)
    }
}