package kr.co.iboss.chat

import android.app.Application
import android.content.Context
import com.kakao.sdk.common.KakaoSdk
import com.sendbird.android.SendBird
import kr.co.iboss.chat.FCM.MyFirebaseMessagingService
import kr.co.iboss.chat.Utils.PushUtils

/* 어플리케이션 실행과 동시에 시작되는 Class */
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

    /* 기본적인 SDK Initialize Method*/
    private fun sdkInitialize() {
        SendBird.init(APP_ID, applicationContext)

        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        // 푸시 디바이스 토큰 등록
        PushUtils.registerPushHandler(MyFirebaseMessagingService())
    }

    /* application context instance 생성 */
    fun context() : Context = applicationContext
}