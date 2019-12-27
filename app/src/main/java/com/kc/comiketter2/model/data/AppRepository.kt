package com.kc.comiketter2.model.data

import com.kc.comiketter2.model.data.room.UserEntity
import com.kc.comiketter2.model.data.room.source.AppLocalDataSource
import com.kc.comiketter2.model.data.room.source.AppLocalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class AppRepository(
  private val appLocalDataSource: AppLocalDataSource
) : AppDataSource, CoroutineScope {

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + Job()

  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>? {
    return appLocalDataSource.searchUsersFromCircleName(circleName)
  }

  override suspend fun searchUsersFromName(name: String): List<UserEntity>? {
    return appLocalDataSource.searchUsersFromName(name)
  }

  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>? {
    return appLocalDataSource.searchUsersFromScreenName(screenName)
  }

  override suspend fun addUser(user: UserEntity) {
    appLocalDataSource.addUser(user)
  }

  override suspend fun addUsers(vararg users: UserEntity) {
    appLocalDataSource.addUsers(*users)
  }

  override suspend fun updateUser(user: UserEntity) {
    appLocalDataSource.updateUser(user)
  }

  override suspend fun updateUsers(vararg users: UserEntity) {
    appLocalDataSource.updateUsers(*users)
  }

  override suspend fun deleteUser(user: UserEntity) {
    appLocalDataSource.deleteUser(user)
  }

  override suspend fun deleteUsers(vararg users: UserEntity) {
    appLocalDataSource.deleteUsers(*users)
  }

  override suspend fun deleteAllUsers() {
    appLocalDataSource.deleteAllUsers()
  }

  companion object {
    private var INSTANCE: AppRepository? = null
    private val lock = Any()

    fun getInstance(
      localDataSource: AppLocalDataSource
    ): AppRepository =
      INSTANCE ?: synchronized(lock) {
        INSTANCE ?: AppRepository(localDataSource)
          .also { INSTANCE = it }
      }

    fun destroyInstance() {
      AppLocalDatabase.destroyInstance()
      AppLocalDataSource.destroyInstance()
      INSTANCE = null
    }
  }
}