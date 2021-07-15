package com.shong.practice_encryption

import android.content.Context
import android.content.SharedPreferences


class MyPreferences(context : Context) : PrefInterface{
    private val pref : SharedPreferences = context.getSharedPreferences("test", Context.MODE_PRIVATE)

    override fun setId(id: String) = pref.edit().putString("id",id).apply()

    override fun getId() : String = pref.getString("id","")!!

    override fun setPassword(pwd: String) = pref.edit().putString("pwd",pwd).apply()

    override fun getPassword(): String = pref.getString("pwd","")!!

    override fun setTokenSalt(salt: String) = pref.edit().putString("salt", salt).apply()

    override fun getTokenSalt(): String  = pref.getString("salt", "")!!

    override fun setTokenIv(iv: String) = pref.edit().putString("iv", iv).apply()

    override fun getTokenIv(): String  = pref.getString("iv", "")!!

    override fun setTokenEncrypted(encrypted: String) = pref.edit().putString("encrypted", encrypted).apply()

    override fun getTokenEncrypted(): String = pref.getString("encrypted", "")!!

    override fun setToken(token: String) = pref.edit().putString("token", token).apply()

    override fun getToken(): String = pref.getString("token", "")!!

    override fun setAutoLogin(auto: Boolean) = pref.edit().putBoolean("auto", auto).apply()

    override fun getAutoLogin() = pref.getBoolean("auto",false)!!
}