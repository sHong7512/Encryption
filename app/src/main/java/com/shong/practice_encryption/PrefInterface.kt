package com.shong.practice_encryption

interface PrefInterface{
    fun setId(id: String)
    fun getId(): String

    fun setPassword(pwd: String)
    fun getPassword(): String

    fun setTokenSalt(salt: String)
    fun getTokenSalt(): String

    fun setTokenIv(iv: String)
    fun getTokenIv(): String

    fun setTokenEncrypted(encrypted: String)
    fun getTokenEncrypted(): String

    fun setToken(token: String)
    fun getToken() : String

    fun setAutoLogin(auto: Boolean)
    fun getAutoLogin(): Boolean
}