package com.kc.comiketter2.domain.interactor

import com.kc.comiketter2.domain.usecase.SearchUsersUseCase
import com.kc.comiketter2.model.data.room.UserEntity

class SearchUsersInteractor : SearchUsersUseCase {
  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun searchUsersFromName(name: String): List<UserEntity>? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}