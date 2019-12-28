package com.kc.comiketter2.domain.usecase.data

import com.kc.comiketter2.model.data.room.UserEntity

interface SearchUsersUseCase {
  suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>?
  suspend fun searchUsersFromName(name: String): List<UserEntity>?
  suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>?
}