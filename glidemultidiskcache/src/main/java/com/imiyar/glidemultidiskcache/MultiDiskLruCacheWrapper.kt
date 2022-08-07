package com.imiyar.glidemultidiskcache

import android.util.Log
import android.util.SparseArray
import com.bumptech.glide.disklrucache.DiskLruCache
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator
import java.io.File
import java.io.IOException

/**
 * Extended disk LRU cache, support setting multiple cache directories.
 *
 * Refer to [com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper]
 *
 * @author Xeemoo
 */
class MultiDiskLruCacheWrapper private constructor(
    private val cacheDirConfig: Map<Int, Pair<File, Long>>
) : DiskCache {

    private val safeKeyGenerator = SafeKeyGenerator()
    private val writeLocker = DiskCacheWriteLocker()
    private val diskLruCacheMap: SparseArray<DiskLruCache>

    init {
        if (cacheDirConfig.size > MAX_LIMIT) throw Exception("It is recommended not to set too many cache directories.")
        diskLruCacheMap = SparseArray(cacheDirConfig.size)
    }

    override fun get(key: Key?): File? {
        val safeKey = safeKeyGenerator.getSafeKey(key)
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Get: Obtained: $safeKey for for Key: $key")
        }
        var result: File? = null
        try {
            // It is possible that the there will be a put in between these two gets. If so that shouldn't
            // be a problem because we will always put the same value at the same key so our input streams
            // will still represent the same data.
            val value = getDiskCache(key)!![safeKey]
            if (value != null) {
                result = value.getFile(0)
            }
        } catch (e: IOException) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to get from disk cache", e)
            }
        }
        return result
    }

    override fun put(key: Key?, writer: DiskCache.Writer?) {
        // We want to make sure that puts block so that data is available when put completes. We may
        // actually not write any data if we find that data is written by the time we acquire the lock.
        val safeKey = safeKeyGenerator.getSafeKey(key)
        writeLocker.acquire(safeKey)
        try {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Put: Obtained: $safeKey for for Key: $key")
            }
            try {
                // We assume we only need to put once, so if data was written while we were trying to get
                // the lock, we can simply abort.
                val diskCache = getDiskCache(key)
                val current = diskCache!![safeKey]
                if (current != null) {
                    return
                }
                val editor = diskCache.edit(safeKey)
                    ?: throw IllegalStateException("Had two simultaneous puts for: $safeKey")
                try {
                    val file = editor.getFile(0)
                    if (writer!!.write(file)) {
                        editor.commit()
                    }
                } finally {
                    editor.abortUnlessCommitted()
                }
            } catch (e: IOException) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unable to put to disk cache", e)
                }
            }
        } finally {
            writeLocker.release(safeKey)
        }
    }

    override fun delete(key: Key?) {
        val safeKey = safeKeyGenerator.getSafeKey(key)
        try {
            getDiskCache(key)!!.remove(safeKey)
        } catch (e: IOException) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to delete from disk cache", e)
            }
        }
    }

    override fun clear() {
        try {
            getDiskCache(null)!!.delete()
        } catch (e: IOException) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to clear disk cache or disk cache cleared externally", e)
            }
        } finally {
            // Delete can close the cache but still throw. If we don't null out the disk cache here, every
            // subsequent request will try to act on a closed disk cache and fail. By nulling out the disk
            // cache we at least allow for attempts to open the cache in the future. See #2465.
            resetDiskCache()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun getDiskCache(key: Key?): DiskLruCache? {
        var type = cacheDirConfig.entries.first().key

        key?.takeIf {
            key.javaClass.name.endsWith("ResourceCacheKey") || key.javaClass.name.endsWith("DataCacheKey")
        }?.let {
            // 从Key中读取signature标识
            try {
                val field = key.javaClass.getDeclaredField("signature")
                field.isAccessible = true
                val signature = field[key]
                if (signature is MultiGlideCacheSignature) {
                    if (!cacheDirConfig.keys.contains(signature.type)) {
                        throw Exception("The parameter type of method multiCache() must be included in the collection passed in when creating MultiExternalCacheDiskCacheFactory.")
                    }
                    type = signature.type
                }
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }

        return cacheDirConfig[type]?.let { cfg ->
            diskLruCacheMap.get(
                type,
                DiskLruCache.open(cfg.first, APP_VERSION, VALUE_COUNT, cfg.second).also {
                    diskLruCacheMap[type] = it
                }
            )
        }
    }

    @Synchronized
    private fun resetDiskCache() {
        diskLruCacheMap.clear()
    }

    companion object {
        private const val TAG = "MultiDiskLruCache"

        private const val MAX_LIMIT = 4

        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 1

        /**
         * Create a new DiskCache in the given directory with a specified max size.
         *
         * @param cacheDirConfig Multi directory and max size config for the disk cache
         * @return The new disk cache with the given arguments
         */
        @JvmStatic
        @Synchronized
        fun create(cacheDirConfig: Map<Int, Pair<File, Long>>): DiskCache {
            return MultiDiskLruCacheWrapper(cacheDirConfig)
        }
    }
}