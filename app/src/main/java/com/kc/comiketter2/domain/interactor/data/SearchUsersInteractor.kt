package com.kc.comiketter2.domain.interactor.data

import com.kc.comiketter2.domain.usecase.data.SearchUsersUseCase
import com.kc.comiketter2.model.data.AppRepository
import com.kc.comiketter2.model.data.room.UserEntity

class SearchUsersInteractor(private val repository: AppRepository) : SearchUsersUseCase {
  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>? {
    return repository.searchUsersFromScreenName(screenName)
  }

  override suspend fun searchUsersFromName(name: String): List<UserEntity>? {
    return repository.searchUsersFromName(name)
  }

  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>? {
    return repository.searchUsersFromCircleName(circleName)
  }
}