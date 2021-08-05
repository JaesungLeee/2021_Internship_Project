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


object ImageUtils {

    // Prevent instantiation
    private fun ImageUtils() {}

    /**
     * Crops image into a circle that fits within the ImageView.
     */
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

    fun displayImageFromUrl(
        context: Context?, url: String?,
        imageView: ImageView?, placeholderDrawable: Drawable?
    ) {
        displayImageFromUrl(context, url, imageView, placeholderDrawable, null)
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    fun displayImageFromUrl(
        context: Context?, url: String?,
        imageView: ImageView?, placeholderDrawable: Drawable?, listener: RequestListener<*>?
    ) {
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
        } else {
            Glide.with(context!!)
                .load(url)
                .apply(myOptions)
                .listener(listener)
                .into(imageView!!)
        }
    }

    fun displayRoundImageFromUrlWithoutCache(
        context: Context, url: String?,
        imageView: ImageView
    ) {
        displayRoundImageFromUrlWithoutCache(context, url, imageView, null)
    }

    fun displayRoundImageFromUrlWithoutCache(
        context: Context, url: String?,
        imageView: ImageView, listener: RequestListener<*>?
    ) {
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
        } else {
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

    /**
     * Displays an image from a URL in an ImageView.
     * If the image is loading or nonexistent, displays the specified placeholder image instead.
     */
    fun displayImageFromUrlWithPlaceHolder(
        context: Context?, url: String?,
        imageView: ImageView?,
        placeholderResId: Int
    ) {
        val myOptions = RequestOptions()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(placeholderResId)
        Glide.with(context!!)
            .load(url)
            .apply(myOptions)
            .into(imageView!!)
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    fun displayGifImageFromUrl(
        context: Context?,
        url: String?,
        imageView: ImageView?,
        placeholderDrawable: Drawable?,
        listener: RequestListener<*>?
    ) {
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
        } else {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .into(imageView!!)
        }
    }

    /**
     * Displays an GIF image from a URL in an ImageView.
     */
    fun displayGifImageFromUrl(
        context: Context?,
        url: String?,
        imageView: ImageView?,
        thumbnailUrl: String?,
        placeholderDrawable: Drawable?
    ) {
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
                .thumbnail(Glide.with(context!!).asGif().load(thumbnailUrl))
                .into(imageView!!)
        } else {
            Glide.with(context!!)
                .asGif()
                .load(url)
                .apply(myOptions)
                .into(imageView!!)
        }
    }
}