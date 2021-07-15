package com.shong.practice_encryption

import androidx.lifecycle.LiveData
import com.shong.practice_encryption.db.AppDatabase
import com.shong.practice_encryption.db.entity.TokenEntity

class Repository(mDatabase: AppDatabase) {

    private val tokenDao = mDatabase.tokenDao()
    val tokens: LiveData<List<TokenEntity>> = tokenDao.getEncryptedToken()

    companion object{
        private var sInstance: Repository? = null
        fun getInstance(database: AppDatabase): Repository{
            return sInstance
                ?: synchronized(this){
                    val instance = Repository(database)
                    sInstance = instance
                    instance
                }
        }
    }

    suspend fun insert(tokenEntity: TokenEntity){
        tokenDao.insert(tokenEntity)
    }
}