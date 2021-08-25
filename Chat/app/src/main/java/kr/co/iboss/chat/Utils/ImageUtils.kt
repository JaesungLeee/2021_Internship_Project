package kr.co.iboss.chat.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget

/* 이미지 Display 관련 Image Utils*/
object ImageUtils {

    // Prevent instantiation
    private fun ImageUtils() {}

    /* Image를 담고 있는 URL을 이용하여 ImageView에 원형으로 보이는 Method */
    fun displayRoundImageFromUrl(context: Context, url: String?, imageView: ImageView) {
        val myOptions = RequestOptions()
            .centerCrop()
            .dontAnimate()

        Glide.with(context)
            .asBitmap()
            .apply(myOptions)
            .load(url)
            .into(object : BitmapImageViewTarget(imageView) {
                override fun setResource(resource: Bitmap?) {
                    val circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource)
                    circularBitmapDrawable.isCircular = true
                    imageView.setImageDrawable(circularBitmapDrawable)
                }
            })
    }

    fun displayImageFromUrl(context: Context?, url: String?, imageView: ImageView?, placeholderDrawable: Drawable?) {
        displayImageFromUrl(context, url, imageView, placeholderDrawable, null)
    }

    /* Image를 담고 있는 URL을 이용하여 ImageView에 보이는 Method */
    fun displayImageFromUrl(context: Context?, url: String?, imageView: ImageView?, placeholderDrawable: Drawable?, listener: RequestListener<*>?) {
        val myOptions = RequestOptions()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(placeholderDrawable)
        if (listener != null) {
            Glide.with(context!!)
                .load(url)
                .apply(myOptions)
                .listener(listener as RequestListener<Drawable>?)
                .into(imageView!!)
        }
        else {
            Glide.with(context!!)
                .load(url)
                .apply(myOptions)
                .listener(listener)
                .into(imageView!!)
        }
    }

    fun displayRoundImageFromUrlWithoutCache(context: Context, url: String?, imageView: ImageView) {
        displayRoundImageFromUrlWithoutCache(context, url, imageView, null)
    }

    /* Image를 담고있는 URL을 이용하여 캐싱 없이 ImageView에 원형으로 표시하는 Method */
    fun displayRoundImageFromUrlWithoutCache(context: Context, url: String?, imageView: ImageView, listener: RequestListener<*>?) {
        val myOptions = RequestOptions()
            .centerCrop()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

        if (listener != null) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(myOptions)
                .listener(listener as RequestListener<Bitmap>?)
                .into(object : BitmapImageViewTarget(imageView) {
                    override fun setResource(resource: Bitmap?) {
                        val circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource)
                        circularBitmapDrawable.isCircular = true
                        imageView.setImageDrawable(circularBitmapDrawable)
                    }
                })
        }
        else {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(myOptions)
                .into(object : BitmapImageViewTarget(imageView) {
                    override fun setResource(resource: Bitmap?) {
                        val circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource)
                        circularBitmapDrawable.isCircular = true
                        imageView.setImageDrawable(circularBitmapDrawable)
                    }
                })
        }
    }

    /*
     * Image를 담고 있는 URL을 이용하여 ImageView에 표시하는 Method
     * Image가 존재하지 않으면 대체 이미지를 이용하여 표시
     */
    fun displayImageFromUrlWithPlaceHolder(context: Context?, url: String?, imageView: ImageView?, placeholderResId: Int) {
        val myOptions = RequestOptions()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(placeholderResId)

        Glide.with(context!!)
            .load(url)
            .apply(myOptions)
            .into(imageView!!)
    }

    /* GIF Image를 담고있는 URL을 이용하여 ImageView에 표시하는 Method */
    fun displayGifImageFromUrl(context: Context?, url: String?, imageView: ImageView?, placeholderDrawable: Drawable?, listener: RequestListener<*>?) {
        val myOptions = RequestOptions()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .placeholder(placeholderDrawable)

        if (listener != null) {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .listener(listener as RequestListener<GifDrawable>?)
                .into(imageView!!)
        }
        else {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .into(imageView!!)
        }
    }

    fun displayGifImageFromUrl(context: Context?, url: String?, imageView: ImageView?, thumbnailUrl: String?, placeholderDrawable: Drawable?) {
        val myOptions = RequestOptions()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .placeholder(placeholderDrawable)

        if (thumbnailUrl != null) {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                .into(imageView!!)
        }
        else {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .into(imageView!!)
        }
    }
}