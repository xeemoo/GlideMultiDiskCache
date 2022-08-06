package com.imiyar.glidedemo

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.imiyar.glidedemo.custom.MultiDiskLruCacheWrapper
import com.imiyar.glidedemo.custom.MultiExternalCacheDiskCacheFactory
import com.imiyar.glidedemo.custom.MultiExternalCacheDiskCacheFactory.Companion.getCacheDirectory

@GlideModule
class DemoGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSize = 1024 * 1024 * 200L  //200MB

        // cache path: /sdcard/Android/data/<pkgName>/cache/<dir>/
        builder.setDiskCache(
            MultiExternalCacheDiskCacheFactory(mapOf(
                MultiDiskLruCacheWrapper.TYPE_DEFAULT to Pair(getCacheDirectory(context, dir), diskCacheSize),
                MultiDiskLruCacheWrapper.TYPE_SUB_ONE to Pair(getCacheDirectory(context, dir_sub_1), diskCacheSize),
                MultiDiskLruCacheWrapper.TYPE_SUB_TWO to Pair(getCacheDirectory(context, dir_sub_2), diskCacheSize),
                MultiDiskLruCacheWrapper.TYPE_SUB_THREE to Pair(getCacheDirectory(context, dir_sub_3), diskCacheSize)
            ))
        )

        /** And, default behavior same as [com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory] **/
        // builder.setDiskCache(MultiExternalCacheDiskCacheFactory(context, dir, diskCacheSize))
    }

    companion object {
        private const val dir = "DemoGlideCache"
        private const val dir_sub_1 = "DemoGlideCache_1"
        private const val dir_sub_2 = "DemoGlideCache_2"
        private const val dir_sub_3 = "DemoGlideCache_3"
    }
}