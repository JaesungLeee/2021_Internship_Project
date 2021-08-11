package kr.co.iboss.ibosschat

import android.app.Application

class BaseApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        baseInitialize()
    }

    private fun baseInitialize() {
        PushUtils.registerPushHandler(WebFCMServices())

    }
}