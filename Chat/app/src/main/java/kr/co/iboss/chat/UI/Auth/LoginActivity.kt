package kr.co.iboss.chat.UI.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.sendbird.android.SendBird
import kr.co.iboss.chat.HTTP.DTO.EmailRequestDTO
import kr.co.iboss.chat.HTTP.LoginResponse
import kr.co.iboss.chat.HTTP.RetrofitClient
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.Utils.PreferencesUtils
import kr.co.iboss.chat.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        private val INTENT_USER_ID              = "INTENT_USER_ID"
    }

    private lateinit var binding     : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        buttonHandler()
    }

    private fun buttonHandler() {
        binding.loginBtn.setOnClickListener {
            val inputId = binding.loginIdET.text.toString()
            val inputPW = binding.loginPWET.text.toString()


//            val loginRequestParams : HashMap<String, String> = HashMap()
//            loginRequestParams["id"] = inputId
//            loginRequestParams["pw"] = inputPW

            val loginRequestParams = EmailRequestDTO( id = inputId, pw= inputPW)

            Log.e("PARAMS2", loginRequestParams.toString())
            if (!TextUtils.isEmpty(inputId) && !TextUtils.isEmpty(inputPW)) {
                Log.e("LOGIN_RETROFIT", "START ACTION")

                RetrofitClient.instance.userLogin(loginRequestParams)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            Log.e("First", response.body().toString())
                            if (response.isSuccessful) {
                                Log.e("C", response.code().toString())
                                Log.e("D", response.message().toString())
                                Log.e("RESPONSE", response.body().toString())
                                if (response.code() == 200) {
                                    val loginResponse = response.body()

                                    Log.e("RESPONSE", loginResponse.toString())
                                    val userID = loginResponse?.data?.userID!!
                                    val userNickName = loginResponse.data.userNickName
                                    val userProfileImage = loginResponse.data.userProfileImage

                                    setUserInfo(userID, userNickName, userProfileImage)
                                    connectWithSendBird(userID, userNickName, userProfileImage)
                                }
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "잠시 후에 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            else {
                if (TextUtils.isEmpty(inputId)) Toast.makeText(this, "아이디를 입력하세요", Toast.LENGTH_LONG).show()
                else Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_LONG).show()
            }
        }

        binding.navigateBeforeBtn.setOnClickListener {
            val intent = Intent(this, LoginHomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setUserInfo(userId: String, userNickName: String, userProfileImg : String) {
        PreferencesUtils(this).apply {
            setUserId(userId)
            setUserNickName(userNickName)
            setUserProfileImg(userProfileImg)
        }
    }

    private fun connectWithSendBird(userId : String, userNickName: String, userProfileImg: String) {
        SendBird.connect(userId) { user, e ->
            if (e != null) {
                Log.e("SENDBIRD_CONNECT_ERR", "Code - ${e.code} \nMessage - ${e.message}")
            }

            SendBird.updateCurrentUserInfo(userNickName, userProfileImg) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(INTENT_USER_ID, userId)
                }
                startActivity(intent)
                finish()
            }
        }

    }
}