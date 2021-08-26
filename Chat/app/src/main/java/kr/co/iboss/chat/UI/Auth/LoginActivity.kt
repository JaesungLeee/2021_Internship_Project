package kr.co.iboss.chat.UI.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.sendbird.android.SendBird
import kr.co.iboss.chat.FCM.MyFirebaseMessagingService
import kr.co.iboss.chat.HTTP.DTO.EmailRequestDTO
import kr.co.iboss.chat.HTTP.LoginResponse
import kr.co.iboss.chat.HTTP.RetrofitClient
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.PreferencesUtils
import kr.co.iboss.chat.Utils.PushUtils
import kr.co.iboss.chat.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* 일반 계정 로그인 Activity */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding     : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        buttonHandler()
    }

    private fun buttonHandler() {
        /* 로그인 버튼 클릭 리스너*/
        binding.loginBtn.setOnClickListener {
            val inputId = binding.loginIdET.text.toString()
            val inputPW = binding.loginPWET.text.toString()


//            val loginRequestParams : HashMap<String, String> = HashMap()
//            loginRequestParams["id"] = inputId
//            loginRequestParams["pw"] = inputPW

            val loginRequestParams = EmailRequestDTO( id = inputId, pw= inputPW)    // RequestDTO를 Request Parameter로 생성

            if (!TextUtils.isEmpty(inputId) && !TextUtils.isEmpty(inputPW)) {
                Log.e("LOGIN_RETROFIT", "START ACTION")

                RetrofitClient.instance.userLogin(loginRequestParams)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            if (response.isSuccessful) {    // Request 성공 후 Response 수신
                                if (response.code() == 200) {   // 정상 응답
                                    val loginResponse = response.body()

                                    Log.e("RESPONSE", loginResponse.toString())
                                    val userID = loginResponse?.data?.userID!!
                                    val userNickName = loginResponse.data.userNickName
                                    val userProfileImage = loginResponse.data.userProfileImage


                                    connectWithSendBird(userID, userNickName, userProfileImage)
                                }
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {   // Request 실패
                            Toast.makeText(this@LoginActivity, "잠시 후에 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            else {
                if (TextUtils.isEmpty(inputId)) Toast.makeText(this, "아이디를 입력하세요", Toast.LENGTH_LONG).show()
                else Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_LONG).show()
            }
        }

        binding.navigateBeforeBtn.setOnClickListener {
            val intent = Intent(this, LoginHomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    /* SendBird 서버 연결 Method */
    private fun connectWithSendBird(userId : String, userNickName: String, userProfileImg: String) {
        ConnectionUtils.login(userId) { user, e ->
            if (e != null) {
                Log.e("SENDBIRD_CONNECT_ERR", "Code - ${e.code} \nMessage - ${e.message}")
            }

            PreferencesUtils(this).setConnected(true)   // 로그인 상태 설정 : True
            PushUtils.registerPushHandler(MyFirebaseMessagingService())     // Push Handler 등록
            updateCurrentUserInfo(userId, userNickName, userProfileImg)

        }
    }

    /* User 기본 정보 디바이스 내 저장 Method */
    private fun setUserInfo(userId: String, userNickName: String, userProfileImg : String) {
        PreferencesUtils(this).apply {
            setUserId(userId)
            setUserNickName(userNickName)
            setUserProfileImg(userProfileImg)
        }
    }

    /* User 정보 업데이트 Method */
    private fun updateCurrentUserInfo(userId: String, userNickName: String, userProfileImg: String) {
        SendBird.updateCurrentUserInfo(userNickName, userProfileImg) {

            setUserInfo(userId, userNickName, userProfileImg)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}