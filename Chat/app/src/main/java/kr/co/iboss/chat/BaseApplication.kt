package kr.co.iboss.chat

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.sendbird.android.SendBird

class BaseApplication : Application() {
    companion object {
        val APP_ID = "58427C21-8C26-4B1B-B13F-4159BF7D77AE"
    }


    override fun onCreate() {
        super.onCreate()
        sdkInitialize()
    }

    private fun sdkInitialize() {
        SendBird.init(APP_ID, applicationContext)

        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }
}