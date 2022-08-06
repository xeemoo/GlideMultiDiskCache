package com.imiyar.glidedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imiyar.glidedemo.custom.MultiDiskLruCacheWrapper.Companion.TYPE_SUB_ONE
import com.imiyar.glidedemo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlideApp.with(this).load(url).into(binding.ivImageView)

        GlideApp.with(this)
            .load(url)
            .multiCache(TYPE_SUB_ONE)
            .into(binding.ivImageViewDir1)
    }

    companion object {
        private const val url = "https://developer.android.google.cn/about/versions/13/images/android-13-hero_720.png"
    }
}