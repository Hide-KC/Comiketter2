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
      INSTANCE = null
    }
  }
}