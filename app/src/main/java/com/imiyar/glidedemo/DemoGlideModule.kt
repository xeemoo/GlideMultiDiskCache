package com.imiyar.glidedemo

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class DemoGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSize = 1024 * 1024 * 200L  //200MB
        // cache path: /sdcard/Android/data/<pkgName>/cache/<dir>/
        builder.setDiskCache(
            ExternalPreferredCacheDiskCacheFactory(context, dir, diskCacheSize)
        )
    }

    companion object {
        private const val dir = "DemoGlideCache"
    }
}