package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.SendBirdPushHelper

object PushUtils {

    fun registerPushHandler(handler : SendBirdPushHandler) {
        SendBirdPushHelper.registerPushHandler(handler)
    }

    fun unregisterPushHandler(listener : SendBirdPushHelper.OnPushRequestCompleteListener) {
        SendBirdPushHelper.unregisterPushHandler(listener)
    }

    fun setPushNotification(enable : Boolean, handler : SendBird.SetPushTriggerOptionHandler) {
        val option = if (enable) SendBird.PushTriggerOption.ALL else SendBird.PushTriggerOption.OFF
        SendBird.setPushTriggerOption(option, handler)
    }
}