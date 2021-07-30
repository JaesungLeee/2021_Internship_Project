package kr.co.iboss.chat.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.sendbird.android.SendBird
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.Auth.LoginHomeActivity
import kr.co.iboss.chat.Utils.PreferencesUtils

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private val INTENT_USER_ID              = "INTENT_USER_ID"
    }

    lateinit var handler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        handler = Handler()
        handler.postDelayed({
            autoLogin()
        }, 3000)
    }

    private fun autoLogin() {
        val preference = PreferencesUtils(this)
        val userId = preference.getUserId()

        if (!TextUtils.isEmpty(userId)) {
            SendBird.connect(userId) { user, e ->
                if (e != null) {
                    Log.e("SENDBIRD_CONNECT_ERR", "Code - ${e.code} \nMessage - ${e.message}")
                }
                Log.e("AUTOLOGIN", "SUCCESS")
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra(INTENT_USER_ID, userId)
                })
            }
        }
        else {
            startActivity(Intent(this, LoginHomeActivity::class.java))
        }
    }
}