package kr.co.iboss.ibosschat

import android.app.Application
import com.sendbird.android.SendBird

class BaseApplication : Application(){

    private val APP_ID = "58427C21-8C26-4B1B-B13F-4159BF7D77AE"

    override fun onCreate() {
        super.onCreate()

        baseInitialize()
    }

    private fun baseInitialize() {
        SendBird.init(APP_ID, applicationContext)

        PushUtils.registerPushHandler(WebFCMServices())
    }
}