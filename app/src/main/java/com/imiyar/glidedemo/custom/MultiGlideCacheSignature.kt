package com.imiyar.glidedemo.custom

import com.bumptech.glide.load.Key
import java.security.MessageDigest

/**
 * Includes information about cache type.
 *
 * @author Xeemoo
 */
class MultiGlideCacheSignature(
    val type: Int
) : Key {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "com.imiyar.glidedemo.custom.MultiGlideCacheSignature"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }
}