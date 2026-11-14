package com.bigong.oguri.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSDataWritingFileProtectionComplete
import platform.Foundation.NSFileManager
import platform.Foundation.NSLock
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class)
private object IosTravelRecoveryStorage : TravelRecoveryStorage {
    private val lock = NSLock()

    override fun <Result> access(block: (String?, (String?) -> Unit) -> Result): Result {
        lock.lock()
        try {
            val manager = NSFileManager.defaultManager
            val directory = manager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL
            val file = requireNotNull(directory.URLByAppendingPathComponent("oguri-travel-recovery"))
            val stored = NSString.stringWithContentsOfURL(file, NSUTF8StringEncoding, null)
            return block(stored) { value ->
                if (value == null) {
                    manager.removeItemAtURL(file, null)
                } else {
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    val data = requireNotNull((value as NSString).dataUsingEncoding(NSUTF8StringEncoding))
                    check(data.writeToURL(file, NSDataWritingAtomic or NSDataWritingFileProtectionComplete, null))
                }
            }
        } finally {
            lock.unlock()
        }
    }
}

actual fun travelRecoveryStorage(): TravelRecoveryStorage = IosTravelRecoveryStorage
