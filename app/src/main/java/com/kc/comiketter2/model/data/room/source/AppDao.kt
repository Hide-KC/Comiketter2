package com.kc.comiketter2.model.data.room.source

import androidx.room.*
import com.kc.comiketter2.domain.usecase.data.SearchUsersUseCase
import com.kc.comiketter2.model.data.room.UserEntity

@Dao
interface AppDao : SearchUsersUseCase {
  @Query("SELECT * FROM users WHERE circle_name LIKE :circleName")
  override suspend fun searchUsersFromCircleName(circleName: String): List<UserEntity>?

  @Query("SELECT * FROM users WHERE name LIKE :name")
  override suspend fun searchUsersFromName(name: String): List<UserEntity>?

  @Query("SELECT * FROM users WHERE screen_name LIKE :screenName")
  override suspend fun searchUsersFromScreenName(screenName: String): List<UserEntity>?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun addUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun addUsers(vararg users: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Update
  suspend fun updateUsers(vararg users: UserEntity)

  @Delete
  suspend fun deleteUser(user: UserEntity)

  @Delete
  suspend fun deleteUsers(vararg users: UserEntity)

  @Query("DELETE FROM users")
  suspend fun deleteAllUsers()
}