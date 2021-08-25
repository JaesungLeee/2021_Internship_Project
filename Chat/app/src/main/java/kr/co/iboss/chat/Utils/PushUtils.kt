package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBirdPushHandler
import com.sendbird.android.SendBirdPushHelper

/* Push Notification 사용을 위해 SendBird 서버에 Push 상태를 등록하는 Handler를 구현한  Push Utils */
object PushUtils {
    /* Push 등록 Method */
    fun registerPushHandler(handler : SendBirdPushHandler) {
        SendBirdPushHelper.registerPushHandler(handler)
    }

    /* Push 해제 Method */
    fun unregisterPushHandler(listener : SendBirdPushHelper.OnPushRequestCompleteListener) {
        SendBirdPushHelper.unregisterPushHandler(listener)
    }
}