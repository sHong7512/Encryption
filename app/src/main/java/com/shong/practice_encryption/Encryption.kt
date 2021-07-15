
package com.shong.practice_encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class Encryption {

    fun encrypt(
        dataToEncrypt: ByteArray,
        password: CharArray
    ): HashMap<String, ByteArray> {

        val map = HashMap<String, ByteArray>()

        val random = SecureRandom()
        val salt = ByteArray(256)
        random.nextBytes(salt)

        val pbKeySpec = PBEKeySpec(password, salt, 1324, 256) //iterationCount: 해시 반복 횟수
        val secretKeyFactory =
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1") //"PBKDF2WithHmacSHA1": 알고리즘 이름
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded   //key생성
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val ivRandom = SecureRandom()   //기존변수(random)이 아닌 새로운 SecureRandom 변수를 만들어서 사용해야함
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val cipher =
            Cipher.getInstance("AES/CBC/PKCS7Padding")     //AES모드 선택 및 PKCS7패딩(모든 데이터가 블록 크기에 맞는게 아니므로 남는 공간에 패딩) 선택
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(dataToEncrypt)

        map["salt"] = salt
        map["iv"] = iv
        map["encrypted"] = encrypted

        return map
    }

    fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

        var decrypted: ByteArray? = null

        val salt = map["salt"]
        val iv = map["iv"]
        val encrypted = map["encrypted"]

        val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        decrypted = cipher.doFinal(encrypted)


        return decrypted
    }

    private fun _keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {

        val map = HashMap<String, ByteArray>()

        //키스토어에서 키를 가져옴
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry =
            keyStore.getEntry("UserToken", null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        //Encrypt data
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(dataToEncrypt)

        map["iv"] = ivBytes
        map["encrypted"] = encryptedBytes

        return map
    }

    private fun _keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {

        var decrypted: ByteArray? = null

        //키스토어에서 키를 가져옴
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry = keyStore.getEntry("UserToken", null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        //Extract info from map
        val encryptedBytes = map["encrypted"]
        val ivBytes = map["iv"]


        //Decrypt data
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        decrypted = cipher.doFinal(encryptedBytes)

        return decrypted
    }

    companion object{
        lateinit private var keyGenerator: KeyGenerator
        lateinit private var keyGenParameterSpec: KeyGenParameterSpec

    }

    init {
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "UserToken",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            //.setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
            //.setUserAuthenticationValidityDurationSeconds(120) // 3 only available x seconds from password authentication. -1 requires finger print - every time
            .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun keystoreEncrypt(TOKEN: String) = _keystoreEncrypt(TOKEN.toByteArray(Charsets.UTF_8))

    fun keystoreDecrypt(map : HashMap<String, ByteArray>) : String{
        val decryptedBytes = _keystoreDecrypt(map)
        var decryptedString = ""
        decryptedBytes?.let { decryptedString = String(it, Charsets.UTF_8) }
        return decryptedString
    }

}

