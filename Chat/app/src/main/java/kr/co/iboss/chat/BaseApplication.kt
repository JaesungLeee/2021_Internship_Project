package kr.co.iboss.chat

import android.app.Application
import android.content.Context
import com.kakao.sdk.common.KakaoSdk
import com.sendbird.android.SendBird
import kr.co.iboss.chat.FCM.MyFirebaseMessagingService
import kr.co.iboss.chat.Utils.PushUtils

class BaseApplication : Application() {
    companion object {
        val APP_ID = "58427C21-8C26-4B1B-B13F-4159BF7D77AE"
        lateinit var instance : BaseApplication
        private set
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        sdkInitialize()
    }

    private fun sdkInitialize() {
        SendBird.init(APP_ID, applicationContext)

        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        PushUtils.registerPushHandler(MyFirebaseMessagingService())
    }

    fun context() : Context = applicationContext
}