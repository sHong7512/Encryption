package com.shong.practice_encryption.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shong.practice_encryption.db.entity.TokenEntity

@Dao
interface TokenDao {
    @Query("SELECT * from tokenTable ORDER BY encrypted ASC")
    fun getEncryptedToken() : LiveData<List<TokenEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tokenEntity: TokenEntity)

    @Query("DELETE FROM tokenTable")
    suspend fun deleteAll()
}