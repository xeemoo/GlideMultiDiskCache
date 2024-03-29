package com.imiyar.glidemultidiskcache

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
    
    override fun equals(other: Any?): Boolean {
        if (other is MultiGlideCacheSignature) {
            return type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        return type
    }

    companion object {
        private const val ID = "com.imiyar.glidemultidiskcache.MultiGlideCacheSignature"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }
}
