package com.imiyar.glidedemo.custom

import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.request.BaseRequestOptions

/**
 * Glide extension for multi disk cache.
 *
 * @author Xeemoo
 */
@GlideExtension
class MultiDiskCacheExtension private constructor() {

    companion object {

        /**
         * Specify cache directory
         */
        @JvmStatic
        @JvmOverloads
        @GlideOption
        fun multiCache(options: BaseRequestOptions<*>, type: Int = 0): BaseRequestOptions<*>? {
            return try {
                options.signature(MultiGlideCacheSignature(type))
            } catch (e: Throwable) {
                options
            }
        }
    }

}