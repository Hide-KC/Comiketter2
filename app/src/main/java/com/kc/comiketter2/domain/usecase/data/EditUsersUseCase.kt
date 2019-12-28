package com.kc.comiketter2.domain.usecase.data

import com.kc.comiketter2.model.data.room.UserEntity

interface EditUsersUseCase {
  suspend fun addUser(user: UserEntity)
  suspend fun addUsers(vararg users: UserEntity)
  suspend fun updateUser(user: UserEntity)
  suspend fun updateUsers(vararg users: UserEntity)
  suspend fun deleteUser(user: UserEntity)
  suspend fun deleteUsers(vararg users: UserEntity)
  suspend fun deleteAllUsers()
}