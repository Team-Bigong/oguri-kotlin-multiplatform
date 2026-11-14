package com.bigong.oguri.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import java.io.File
import java.io.FileNotFoundException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private object AndroidTravelRecoveryStorage : TravelRecoveryStorage {
    private const val KEY_ALIAS = "oguri-travel-recovery"
    private const val NONCE_SIZE = 12
    private const val TAG_BITS = 128
    private const val CIPHER = "AES/GCM/NoPadding"

    @Synchronized
    override fun <Result> access(block: (String?, (String?) -> Unit) -> Result): Result {
        val context = requireNotNull(OguriPlatformContextHolder.applicationContext)
        val file = AtomicFile(File(context.noBackupFilesDir, "travel-recovery"))
        val stored =
            try {
                file.readFully().let { bytes ->
                    require(bytes.size > NONCE_SIZE)
                    val cipher = Cipher.getInstance(CIPHER)
                    cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes.copyOf(NONCE_SIZE)))
                    cipher.doFinal(bytes, NONCE_SIZE, bytes.size - NONCE_SIZE).decodeToString()
                }
            } catch (_: FileNotFoundException) {
                null
            } catch (_: GeneralSecurityException) {
                file.delete()
                null
            } catch (_: IllegalArgumentException) {
                file.delete()
                null
            }

        return block(stored) { value ->
            if (value == null) {
                file.delete()
            } else {
                val cipher = Cipher.getInstance(CIPHER)
                cipher.init(Cipher.ENCRYPT_MODE, key())
                val output = file.startWrite()
                try {
                    output.write(cipher.iv)
                    output.write(cipher.doFinal(value.encodeToByteArray()))
                    file.finishWrite(output)
                } catch (failure: Exception) {
                    file.failWrite(output)
                    throw failure
                }
            }
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec
                        .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build(),
                )
            }.generateKey()
    }
}

actual fun travelRecoveryStorage(): TravelRecoveryStorage = AndroidTravelRecoveryStorage
