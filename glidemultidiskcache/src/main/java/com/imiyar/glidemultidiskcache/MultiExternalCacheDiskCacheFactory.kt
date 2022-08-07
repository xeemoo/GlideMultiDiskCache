package com.imiyar.glidemultidiskcache

import android.content.Context
import com.bumptech.glide.load.engine.cache.DiskCache
import java.io.File

/**
 * Refer to [com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory]
 *
 * @author Xeemoo
 */
class MultiExternalCacheDiskCacheFactory(
    private val cacheConfig: Map<Int, Pair<File, Long>>
) : DiskCache.Factory {

    @JvmOverloads constructor(
        context: Context,
        diskCacheName: String = DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
        diskCacheSize: Long = DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong()
    ) : this(
        mapOf(
            TYPE_DEFAULT to Pair(getCacheDirectory(context, diskCacheName), diskCacheSize)
        )
    )

    init {
        if (cacheConfig.isEmpty()) {
            throw Exception("Disk cache config cannot be empty.")
        }
    }

    override fun build(): DiskCache? {
        var checkDir = true
        cacheConfig.forEach {
            checkDir = checkDir && (it.value.first.isDirectory || it.value.first.mkdir())
        }
        return if (checkDir) {
            MultiDiskLruCacheWrapper.create(cacheConfig)
        } else {
            null
        }
    }

    companion object {
        private const val TYPE_DEFAULT = 0

        @JvmStatic
        fun getCacheDirectory(context: Context, diskCacheName: String?): File {
            val internalCacheDirectory = getInternalCacheDirectory(context, diskCacheName)

            // Already used internal cache, so keep using that one,
            // thus avoiding using both external and internal with transient errors.
            if (null != internalCacheDirectory && internalCacheDirectory.exists()) {
                return internalCacheDirectory
            }

            val cacheDirectory = context.externalCacheDir

            // Shared storage is not available.
            if (cacheDirectory == null || !cacheDirectory.canWrite()) {
                return internalCacheDirectory!!
            }
            return if (diskCacheName != null) {
                File(cacheDirectory, diskCacheName)
            } else cacheDirectory
        }

        private fun getInternalCacheDirectory(context: Context, diskCacheName: String?): File? {
            val cacheDirectory: File = context.cacheDir ?: return null

            return if (diskCacheName != null) {
                File(cacheDirectory, diskCacheName)
            } else cacheDirectory
        }
    }

}