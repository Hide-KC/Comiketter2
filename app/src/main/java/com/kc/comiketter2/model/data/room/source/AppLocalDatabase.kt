package com.kc.comiketter2.model.data.room.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kc.comiketter2.model.data.room.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppLocalDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    private var INSTANCE: AppLocalDatabase? = null
    private val lock = Any()

    fun getInstance(context: Context): AppLocalDatabase =
      INSTANCE ?: synchronized(lock) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          AppLocalDatabase::class.java, "AppLocal.db"
        )
          .build()
          .also { INSTANCE = it }
      }

    fun destroyInstance() {
      INSTANCE = null
    }
  }
}