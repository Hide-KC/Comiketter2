package com.kc.comiketter2.domain.interactor.data

import com.kc.comiketter2.domain.usecase.data.EditUsersUseCase
import com.kc.comiketter2.model.data.AppRepository
import com.kc.comiketter2.model.data.room.UserEntity

class EditUsersInteractor(private val repository: AppRepository) : EditUsersUseCase {
  override suspend fun addUser(user: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun addUsers(vararg users: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun updateUser(user: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun updateUsers(vararg users: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun deleteUser(user: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun deleteUsers(vararg users: UserEntity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun deleteAllUsers() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}