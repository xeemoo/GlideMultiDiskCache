package com.imiyar.glidedemo

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.imiyar.glidemultidiskcache.MultiExternalCacheDiskCacheFactory
import com.imiyar.glidemultidiskcache.MultiExternalCacheDiskCacheFactory.Companion.getCacheDirectory

// define cache types
const val TYPE_IMAGE = 1
const val TYPE_ANIM = 2
const val TYPE_VIDEO = 3
const val TYPE_OTHERS = 4


@GlideModule
class DemoGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSize = 1024 * 1024 * 200L  //200MB
        val diskCacheSize2 = 1024 * 1024 * 1000L  //1000MB

        // cache path: /sdcard/Android/data/<pkgName>/cache/<dir>/
        builder.setDiskCache(
            MultiExternalCacheDiskCacheFactory(
                mapOf(
                    TYPE_IMAGE to Pair(
                        getCacheDirectory(context, dir),
                        diskCacheSize
                    ),
                    TYPE_ANIM to Pair(
                        getCacheDirectory(context, dir_sub_1),
                        diskCacheSize
                    ),
                    TYPE_VIDEO to Pair(
                        getCacheDirectory(context, dir_sub_2),
                        diskCacheSize2
                    ),
                    TYPE_OTHERS to Pair(
                        getCacheDirectory(context, dir_sub_3),
                        diskCacheSize
                    )
                )
            )
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