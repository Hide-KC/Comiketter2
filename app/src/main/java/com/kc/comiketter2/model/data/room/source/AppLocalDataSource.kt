package com.kc.comiketter2.model.data.room.source

import com.kc.comiketter2.model.data.AppDataSource
import com.kc.comiketter2.model.data.room.UserEntity

class AppLocalDataSource(private val appDao: AppDao) : AppDataSource {
  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>? {
    return appDao.searchUsersFromScreenName(screenName)
  }

  override suspend fun searchUsersFromName(name: String): List<UserEntity>? {
    return appDao.searchUsersFromName(name)
  }

  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>? {
    return appDao.searchUsersFromCircleName(circleName)
  }

  companion object {
    private var INSTANCE: AppLocalDataSource? = null
    private val lock = Any()

    fun getInstance(appDao: AppDao): AppLocalDataSource =
      INSTANCE ?: synchronized(lock) {
        INSTANCE ?: AppLocalDataSource(appDao)
          .also {
            INSTANCE = it
          }
      }

    fun destroyInstance() {
      INSTANCE = null
    }
  }
}