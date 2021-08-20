package kr.co.iboss.ibosschat.UI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kr.co.iboss.ibosschat.WebAppInterface
import kr.co.iboss.ibosschat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        fun checkInternet(context: Context): Boolean {
            var status = false
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connectivity != null) {
                val networkInfo = connectivity.allNetworkInfo

                if (networkInfo != null) {
                    for (i in networkInfo.indices) {
                        if (networkInfo[i].state == NetworkInfo.State.CONNECTED) {
                            status = true
                            break
                        }
                    }
                }

            }
            return status
        }
    }

    private var LOG_TAG = "MainActivity :"
    private var EXTRA_URL = "groupChannelUrl"
    private var URL = "https://chat.i-boss.co.kr/"

    private lateinit var mContext : Context
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mWebView: WebView
    private lateinit var mProgressBar : ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mBinding.root
        setContentView(view)


        mContext = this
        mWebView = mBinding.ibossWV
        mProgressBar = mBinding.progressBar

        val extraBundle = intent.extras
        if (extraBundle != null) {
            if (extraBundle.getString(EXTRA_URL) != null && !extraBundle.getString(EXTRA_URL).equals("")) {
                URL = "https://chat.i-boss.co.kr/chatting/${extraBundle.getString(EXTRA_URL)}/false"
                Log.e(LOG_TAG, "FCM URL")
            }
        }


        requestWebView()


    }

    override fun onDestroy() {
        URL = "https://chat.i-boss.co.kr/"
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack()
            return true
        }

        return true
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun requestWebView(){

        val bridge = WebAppInterface(mContext)

        if (checkInternet(mContext)) {
            Log.e(LOG_TAG, "loading")
            mWebView.loadUrl(URL)
        }
        else {
            mProgressBar.visibility = View.GONE
            mWebView.visibility = View.GONE

            return
        }

        mWebView.isFocusable = true
        mWebView.isFocusableInTouchMode = true
        mWebView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        mWebView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            domStorageEnabled = true
            setAppCacheEnabled(true)
            databaseEnabled = true
            setSupportMultipleWindows(false)
        }
        mWebView.addJavascriptInterface(bridge, "AndroidBridge")

        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {

                Log.e(LOG_TAG, "URL : ${url!!}")

                if (checkInternet(mContext)) {
                    view.loadUrl(url)
                }
                else {
                    mProgressBar.visibility = View.GONE
                    mWebView.visibility = View.GONE
                }

                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Log.e("onPageStarted :", "URL : $url")
                super.onPageStarted(view, url, favicon)
                if (mProgressBar.visibility == View.GONE) {
                    mProgressBar.visibility = View.VISIBLE
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("onPageFinished :", "URL : $url")
                super.onPageFinished(view, url)

            }

            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
            }
        }
    }

}