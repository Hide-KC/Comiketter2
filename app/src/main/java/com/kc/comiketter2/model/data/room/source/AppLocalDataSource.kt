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

  override suspend fun addUser(user: UserEntity) {
    appDao.addUser(user)
  }

  override suspend fun addUsers(vararg users: UserEntity) {
    appDao.addUsers(*users)
  }

  override suspend fun updateUser(user: UserEntity) {
    appDao.updateUser(user)
  }

  override suspend fun updateUsers(vararg users: UserEntity) {
    appDao.updateUsers(*users)
  }

  override suspend fun deleteUser(user: UserEntity) {
    appDao.deleteUser(user)
  }

  override suspend fun deleteUsers(vararg users: UserEntity) {
    appDao.deleteUsers(*users)
  }

  override suspend fun deleteAllUsers() {
    appDao.deleteAllUsers()
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