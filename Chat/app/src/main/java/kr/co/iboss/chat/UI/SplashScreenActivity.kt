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

    lateinit var handler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        // 최근 실행 앱 목록에서 실행되었을 경우 gropuChannelUrl을 제거 즉, Push 상태일 때만 groupChannelurl 사용
        if (intent != null && intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra("groupChannelUrl")
        }

        handler = Handler()
        handler.postDelayed({
            autoLogin()
        }, 3000)
    }

    /* 자동 로그인 처리 */
    private fun autoLogin() {
        val preference = PreferencesUtils(this)
        val userId = preference.getUserId()

        /* 로그인 상태이고 저장되어있는 userId가 있을 때 */
        if (ConnectionUtils.isLogin() && !TextUtils.isEmpty(userId)) {
            ConnectionUtils.login(userId) { user, e ->
                if (e != null) {
                    Log.e("SENDBIRD_CONNECT_ERR", "Code - ${e.code} \nMessage - ${e.message}")
                }

                Log.e("AUTOLOGIN", "SUCCESS")
                startActivity(handleNextIntent())   // handleNextIntent( )에서 login 상태 검사
                finish()
            }
        }
        else {
            startActivity(handleNextIntent())
            finish()
        }
    }

    /* SplashScreen 이후 다음 UI 전환 처리 */
    private fun handleNextIntent(): Intent {
        if (ConnectionUtils.isLogin()) {
            val intent = Intent(this, MainActivity::class.java)     // 로그인 상태일 때 MainActivity로 전환
            if (getIntent().hasExtra("groupChannelUrl")) {      // 푸시 메시지 클릭시 intent에 groupChannelUrl 담아 같이 전달
                intent.putExtra("groupChannelUrl", getIntent().getStringExtra("groupChannelUrl"))
            }
            return intent
        }
        return Intent(this, LoginHomeActivity::class.java)
    }
}