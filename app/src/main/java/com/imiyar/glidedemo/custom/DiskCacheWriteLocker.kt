package com.imiyar.glidedemo.custom

import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Synthetic
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/**
 * Keeps a map of keys to locks that allows locks to be removed from the map when no longer in use
 * so the size of the collection is bounded.
 *
 *
 * This class will be accessed by multiple threads in a thread pool and ensures that the number
 * of threads interested in each lock is updated atomically so that when the count reaches 0, the
 * lock can safely be removed from the map.
 *
 * copy from [com.bumptech.glide.load.engine.cache.DiskCacheWriteLocker]
 *
 * @author Xeemoo
 */
internal class DiskCacheWriteLocker {

    private val locks: MutableMap<String, WriteLock?> = HashMap()
    private val writeLockPool = WriteLockPool()

    fun acquire(safeKey: String) {
        var writeLock: WriteLock?
        synchronized(this) {
            writeLock = locks[safeKey]
            if (writeLock == null) {
                writeLock = writeLockPool.obtain()
                locks[safeKey] = writeLock
            }
            writeLock!!.interestedThreads++
        }
        writeLock!!.lock.lock()
    }

    fun release(safeKey: String) {
        var writeLock: WriteLock
        synchronized(this) {
            writeLock = Preconditions.checkNotNull(locks[safeKey])
            check(writeLock.interestedThreads >= 1) {
                ("Cannot release a lock that is not held"
                        + ", safeKey: "
                        + safeKey
                        + ", interestedThreads: "
                        + writeLock.interestedThreads)
            }
            writeLock.interestedThreads--
            if (writeLock.interestedThreads == 0) {
                val removed = locks.remove(safeKey)
                check(removed == writeLock) {
                    ("Removed the wrong lock"
                            + ", expected to remove: "
                            + writeLock
                            + ", but actually removed: "
                            + removed
                            + ", safeKey: "
                            + safeKey)
                }
                writeLockPool.offer(removed)
            }
        }
        writeLock.lock.unlock()
    }

    private class WriteLock @Synthetic internal constructor() {
        val lock: Lock = ReentrantLock()
        var interestedThreads = 0
    }

    private class WriteLockPool @Synthetic internal constructor() {
        private val pool: Queue<WriteLock?> = ArrayDeque()

        fun obtain(): WriteLock {
            var result: WriteLock?
            synchronized(pool) {
                result = pool.poll()
            }
            return result ?: WriteLock()
        }

        fun offer(writeLock: WriteLock?) {
            synchronized(pool) {
                if (pool.size < MAX_POOL_SIZE) {
                    pool.offer(writeLock)
                }
            }
        }

        companion object {
            private const val MAX_POOL_SIZE = 10
        }
    }
}