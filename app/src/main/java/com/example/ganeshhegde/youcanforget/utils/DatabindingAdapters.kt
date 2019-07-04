package com.example.ganeshhegde.youcanforget.utils

import android.databinding.BindingAdapter
import android.media.Image
import android.widget.ImageView
import com.example.ganeshhegde.youcanforget.R
import com.squareup.picasso.Picasso

class DatabindingAdapters {

    companion object {
        @BindingAdapter("bind:image")
        public fun loadImage(imageView:ImageView,url:String)
        {
            if(url!="" && url!=null)
            {
                Picasso.with(imageView.context).load(url).error(R.drawable.nodata).placeholder(R.drawable.nodata).into(imageView)
            }else
            {
                Picasso.with(imageView.context).load(R.drawable.nodata).error(R.drawable.nodata).placeholder(R.drawable.nodata).into(imageView)

            }
        }
    }
}