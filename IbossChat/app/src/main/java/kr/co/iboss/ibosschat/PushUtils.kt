package kr.co.iboss.ibosschat

import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.SendBirdPushHelper

object PushUtils {
    fun registerPushHandler(handler : SendBirdPushHandler) {
        SendBirdPushHelper.registerPushHandler(handler)
    }
}