package kr.co.iboss.chat.UI.Auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.sendbird.android.SendBird
import kr.co.iboss.chat.FCM.MyFirebaseMessagingService
import kr.co.iboss.chat.HTTP.DTO.SocialRequestDTO
import kr.co.iboss.chat.HTTP.LoginResponse
import kr.co.iboss.chat.HTTP.RetrofitClient
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.PreferencesUtils
import kr.co.iboss.chat.Utils.PushUtils
import kr.co.iboss.chat.databinding.ActivityLoginHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/* 소셜 로그인 Activity */
class LoginHomeActivity : AppCompatActivity() {

    companion object {
        private val NAVER_TYPE                  = "naver"
        private val KAKAO_TYPE                  = "kakao"
        private val FACEBOOK_TYPE               = "facebook"
        private val GOOGLE_TYPE                 = "google"
    }

    private var mContext : Context = this
    private lateinit var binding : ActivityLoginHomeBinding

    private var mLoginType = NAVER_TYPE
    private var TOKEN = "TOKEN"

    private lateinit var mOAuthLoginInstance : OAuthLogin
    private lateinit var callbackManager : CallbackManager
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private var RC_SIGN_IN = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("LIFECYCLE", "ONCREATE")

        binding = ActivityLoginHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val naverLoginBtn = binding.naverLoginBtn
        val kakaoLoginBtn = binding.kakaoLoginBtn
        val googleLoginBtn = binding.googleLoginBtn
        val fbLoginBtn = binding.facebookLoginBtn
        val emailLoginBtn = binding.emailLoginBtn

        socialInstanceInitialize(googleLoginBtn)
        buttonListener(naverLoginBtn, kakaoLoginBtn, googleLoginBtn, fbLoginBtn, emailLoginBtn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    /* 소셜 로그인 인스턴스 초기화 Method*/
    private fun socialInstanceInitialize(googleLoginBtn: SignInButton) {
        naverInstanceInitialize()
        facebookInstanceInitialize()
        googleInstanceInitialize(googleLoginBtn)
    }

    private fun naverInstanceInitialize() {
        mOAuthLoginInstance = OAuthLogin.getInstance()
        mOAuthLoginInstance.init(mContext, getString(R.string.naver_client_id), getString(R.string.naver_clien_secret), getString(R.string.naver_client_name))
    }

    private fun facebookInstanceInitialize() {
        callbackManager = CallbackManager.Factory.create()
    }

    private fun googleInstanceInitialize(googleLoginBtn: SignInButton) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)
        val account = GoogleSignIn.getLastSignedInAccount(mContext)

        googleLoginBtn.setSize(SignInButton.SIZE_WIDE)
    }

    /* 버튼 리스너 */
    private fun buttonListener(naverLoginBtn: AppCompatButton, kakaoLoginBtn: AppCompatButton, googleLoginBtn: SignInButton, fbLoginBtn: LoginButton, emailLoginBtn: AppCompatButton) {
        naverLoginBtn.setOnClickListener {
            naverBtnListener()
        }

        kakaoLoginBtn.setOnClickListener {
            kakaoBtnListener()
        }

        fbLoginBtn.setOnClickListener {
            facebookBtnListener(fbLoginBtn)
        }

        googleLoginBtn.setOnClickListener {
            googleBtnListener(googleLoginBtn)
        }

        emailLoginBtn.setOnClickListener {
            emailLoginBtnListener()
        }
    }



    /* 네이버 소셜 로그인 */
    private fun naverBtnListener() {
        val mOAuthLoginHandler : OAuthLoginHandler = object : OAuthLoginHandler() {
            override fun run(success: Boolean) {
                if (success) {
                    val accessToken = mOAuthLoginInstance.getAccessToken(mContext)
                    val refreshToken = mOAuthLoginInstance.getRefreshToken(mContext)
                    val tokenType = mOAuthLoginInstance.getTokenType(mContext)

                    Log.e("TOKEN", "access : $accessToken \nrefresh : $refreshToken \ntokenType : $tokenType")
                }
                else {
                    val errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).code.toString()
                    val errorMsg = mOAuthLoginInstance.getLastErrorDesc(mContext).toString()
                    Log.e("NAVER_ERR", "Error ${errorCode} : $errorMsg")
                }
            }

        }
        mOAuthLoginInstance.startOauthLoginActivity(this, mOAuthLoginHandler)
    }

    /* 카카오 소셜 로그인 */
    private fun kakaoBtnListener() {
        val callback : ((OAuthToken?, Throwable?) -> Unit) = { token, e ->
            if (e != null) {
                when {
                    e.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.ServerError.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    e.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                    else -> { // Unknown
                        Log.e("KAKAO_ERR", "error : ${e.message}")
                    }
                }

            }
            else if (token != null) {
                UserApiClient.instance.me { user, e ->
                    val kakaoID = user!!.id
                    val kakaoEmail = user.kakaoAccount!!.email
                    val kakaoProfile = user.kakaoAccount!!.profile

                    Log.e("KAKAO_SUCCESS", "id : $kakaoID \nkakaoEmail : $kakaoEmail \nkakaoProfile : $kakaoProfile")
                }
            }

        }

        UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)

    }

    /* 페이스북 소셜 로그인 */
    private fun facebookBtnListener(fbLoginBtn: LoginButton) {
        fbLoginBtn.setReadPermissions(listOf("email", "public_profile", "user_gender", "user_birthday", "user_friends"))
        fbLoginBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val accesToken = result?.accessToken.toString()
                Log.e("FB_TOKEN", accesToken)
            }

            override fun onCancel() {
                return
            }

            override fun onError(e: FacebookException?) {
                Log.e("FB_ERR", "Error : ${e?.message}")
            }

        })
    }

    /* 구글 소셜 로그인 */
    private fun googleBtnListener(googleLoginBtn: SignInButton) {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun emailLoginBtnListener() {
        Log.e("ONCLICK", "TRUE")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleGoogleSignInResult(completedTask : Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val id = account?.id

            Log.e("GOOGLE_SIGNIN_OK", "token : $idToken\nid : $id")

            mLoginType = GOOGLE_TYPE
            TOKEN = id.toString()

            retrofitService(mLoginType, TOKEN)

        } catch (e : ApiException) {
            Log.e("GOOGLE_SIGNIN_ERR", "Error ${e.statusCode} : ${e.message}")
        }
    }

    /* 소셜 로그인 정보 (token, type) 전달 Method */
    private fun retrofitService(type : String, accessToken : String) {
        mLoginType = when(type) {
            NAVER_TYPE -> NAVER_TYPE
            KAKAO_TYPE -> KAKAO_TYPE
            FACEBOOK_TYPE -> FACEBOOK_TYPE
            GOOGLE_TYPE -> GOOGLE_TYPE
            else -> return
        }

        val socialLoginParams = SocialRequestDTO(loginType = mLoginType, token = accessToken)

        RetrofitClient.instance.socialLogin(socialLoginParams)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        if (response.code() == 200) {
                            val loginResponse = response.body()

                            Log.e("SOCIAL_RESPONSE", "CODE ${loginResponse?.code} : ${loginResponse?.message}")
                            val userID = loginResponse?.data?.userID!!
                            val userNickName = loginResponse.data.userNickName
                            val userProfileImage = loginResponse.data.userProfileImage

                            connectWithSendBird(userID, userNickName, userProfileImage)
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginHomeActivity, "잠시 후에 다시 시도해주세요.", Toast.LENGTH_LONG).show()
                }

            })
    }

    /* User 기본 정보 디바이스 내 저장 Method */
    private fun setUserInfo(userID: String, userNickName: String, userProfileImage: String) {
        PreferencesUtils(this).apply {
            setUserId(userID)
            setUserNickName(userNickName)
            setUserProfileImg(userProfileImage)
        }
    }

    /* SendBird 서버 연결 Method */
    private fun connectWithSendBird(userID: String, userNickName: String, userProfileImage: String) {
        ConnectionUtils.login(userID) { user, e ->
            if (e != null) {
                Log.e("SENDBIRD_CONNECT_ERR", "Code ${e.code} : ${e.message}")
            }
            PreferencesUtils(this).setConnected(true)
            PushUtils.registerPushHandler(MyFirebaseMessagingService())
            updateCurrentUserInfo(userID, userNickName, userProfileImage)

        }
    }

    /* User 정보 업데이트 Method */
    private fun updateCurrentUserInfo(userID: String, userNickName: String, userProfileImage: String) {
       SendBird.updateCurrentUserInfo(userNickName, userProfileImage) {
           setUserInfo(userID, userNickName, userProfileImage)

           val intent = Intent(this, MainActivity::class.java)
           startActivity(intent)
           finish()
       }

    }
}