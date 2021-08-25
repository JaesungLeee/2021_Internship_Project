package kr.co.iboss.chat.UI.Media

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.ActivityPhotoViewBinding

/* 이미지 파일 메시지를 클릭했을 때 이미지가 보여지는 Activity */
class PhotoViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPhotoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")

        val imageView = binding.mainImageView
        val progressbar = binding.progressBar

        progressbar.visibility = View.VISIBLE

        loadImage(url, type, imageView, progressbar)

    }

    /* gif 이미지와 일반 이미지를 로딩하는 Method */
    private fun loadImage(url: String?, type: String?, imageView: ImageView, progressbar: ContentLoadingProgressBar) {
        if (type != null && type.lowercase().contains("gif")) {
            ImageUtils.displayGifImageFromUrl(this, url, imageView, null, object : RequestListener<Any?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Any?>?, isFirstResource: Boolean): Boolean {
                    progressbar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Any?, model: Any?, target: Target<Any?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progressbar.visibility = View.GONE
                    return false
                }
            })
        } else {
            ImageUtils.displayImageFromUrl(this, url, imageView, null, object : RequestListener<Any?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Any?>?, isFirstResource: Boolean): Boolean {
                    progressbar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Any?, model: Any?, target: Target<Any?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progressbar.visibility = View.GONE
                    return false
                }
            })
        }
    }
}