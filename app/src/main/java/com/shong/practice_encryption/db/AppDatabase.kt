package com.shong.practice_encryption.db

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shong.practice_encryption.Encryption
import com.shong.practice_encryption.db.dao.TokenDao
import com.shong.practice_encryption.db.entity.TokenEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [TokenEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "AngelNet"

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(AppDatabaseCallback(scope))
//                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                INSTANCE?.updateDatabaseCreated(context.applicationContext)
                return instance
            }
        }
    }

    private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch { populateDatabase(database.tokenDao()) }
            }
        }

        suspend fun populateDatabase(tokenDao: TokenDao) {
            tokenDao.deleteAll()
            // Add User
            val token = "store_sHong"
            val encryption = Encryption()
            val encryptToken = encryption.keystoreEncrypt(token)

            tokenDao.insert(
                TokenEntity(
                    encryptToken["encrypted"] ?: byteArrayOf(),
                    encryptToken["iv"] ?: byteArrayOf()
                )
            )

            Log.d("_sHong","insert!")
        }
    }

    // TODO : 아래 코드들은 필요여부에따라 수정하기
    private val mIsDatabaseCreated = MutableLiveData<Boolean>()

    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated()
        }
    }

    private fun setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true)
    }

    open fun getDatabaseCreated(): LiveData<Boolean> {
        return mIsDatabaseCreated
    }
}