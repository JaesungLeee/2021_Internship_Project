package kr.co.iboss.ibosschat

import android.content.Context
import android.os.Handler
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val mContext : Context) {

    private val mHandler = Handler()

    @JavascriptInterface
    fun registerPush() {

        mHandler.post {
            try {
                PushUtils.registerPushHandler(WebFCMServices())
                Toast.makeText(mContext, "PUSH", Toast.LENGTH_LONG).show()
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun unregisterPush() {

    }
}