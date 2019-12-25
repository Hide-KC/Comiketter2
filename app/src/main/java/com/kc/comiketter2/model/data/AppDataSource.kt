package com.kc.comiketter2.model.data

import com.kc.comiketter2.domain.usecase.SearchUsersUseCase
import com.kc.comiketter2.model.data.room.UserEntity

interface AppDataSource : SearchUsersUseCase {
  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>?
  override suspend fun searchUsersFromName(name: String): List<UserEntity>?
  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>?
}