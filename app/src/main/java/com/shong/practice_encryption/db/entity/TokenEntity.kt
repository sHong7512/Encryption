package com.shong.practice_encryption.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokenTable")
data class TokenEntity(
    @PrimaryKey
    @ColumnInfo(name = "encrypted")
    val encrypted : ByteArray,

    @ColumnInfo(name = "iv")
    val iv : ByteArray
)
