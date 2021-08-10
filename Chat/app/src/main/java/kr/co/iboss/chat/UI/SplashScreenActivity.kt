package kr.co.iboss.chat.UI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.Auth.LoginHomeActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.PreferencesUtils


class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private val INTENT_USER_ID              = "INTENT_USER_ID"
    }

    lateinit var handler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if (intent != null && intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra("groupChannelUrl")
        }

        handler = Handler()
        handler.postDelayed({
            autoLogin()
        }, 3000)
    }

    private fun autoLogin() {
        val preference = PreferencesUtils(this)
        val userId = preference.getUserId()

        if (ConnectionUtils.isLogin() && !TextUtils.isEmpty(userId)) {
            ConnectionUtils.login(userId) { user, e ->
                if (e != null) {
                    Log.e("SENDBIRD_CONNECT_ERR", "Code - ${e.code} \nMessage - ${e.message}")
                }

                Log.e("AUTOLOGIN", "SUCCESS")
                startActivity(handleNextIntent())
                finish()
            }
        }
        else {
            startActivity(handleNextIntent())
            finish()
        }
    }

    private fun handleNextIntent(): Intent {
        if (ConnectionUtils.isLogin()) {
            val intent = Intent(this, MainActivity::class.java)
            if (getIntent().hasExtra("groupChannelUrl")) {
                intent.putExtra("groupChannelUrl", getIntent().getStringExtra("groupChannelUrl"))
            }
            return intent
        }
        return Intent(this, LoginHomeActivity::class.java)
    }
}