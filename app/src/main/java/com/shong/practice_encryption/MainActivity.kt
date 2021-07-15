package com.shong.practice_encryption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shong.practice_encryption.databinding.ActivityMainBinding
import com.shong.practice_encryption.db.entity.TokenEntity

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val encryption = Encryption()
        val pref_Global = MyPreferences(applicationContext)
        var storeBtn = findViewById<Button>(R.id.storeButton)
        var loadBtn = findViewById<Button>(R.id.loadButton)
        var checkBtn = findViewById<Button>(R.id.checkButton)
        val token = "shong_token"

        var encryptToken = hashMapOf<String, ByteArray>()
        encryptToken = encryption.keystoreEncrypt(token)

        val login_password = "shong_pass"
        val password = login_password.toCharArray()

        storeBtn.setOnClickListener {
            val tokenByte = token.toByteArray(Charsets.UTF_8)

            val map = Encryption().encrypt(tokenByte, password)

            val valueBase64String = Base64.encodeToString(map["encrypted"], Base64.NO_WRAP)
            val saltBase64String = Base64.encodeToString(map["salt"], Base64.NO_WRAP)
            val ivBase64String = Base64.encodeToString(map["iv"], Base64.NO_WRAP)

            pref_Global.setTokenSalt(saltBase64String)
            pref_Global.setTokenIv(ivBase64String)
            pref_Global.setTokenEncrypted(valueBase64String)

        }


        loadBtn.setOnClickListener {

            val base64Encrypted = pref_Global.getTokenEncrypted()
            val base64Salt = pref_Global.getTokenSalt()
            val base64Iv = pref_Global.getTokenIv()

            //Base64 decode
            val encrypted = Base64.decode(base64Encrypted, Base64.NO_WRAP)
            val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
            val salt = Base64.decode(base64Salt, Base64.NO_WRAP)

            //Decrypt
            val decrypted = Encryption().decrypt(
                hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)

            var lastLoggedIn: String? = null
            decrypted?.let {
                lastLoggedIn = String(it, Charsets.UTF_8)
            }
            Log.d("_sHong", "token: $lastLoggedIn")

        }

        checkBtn.setOnClickListener {
            Log.d("check_sHong","en : ${pref_Global.getTokenEncrypted()}")
            Log.d("check_sHong","iv : ${pref_Global.getTokenIv()}")
        }

    }
}