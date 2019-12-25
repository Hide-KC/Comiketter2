package com.kc.comiketter2.model.data.room.source

import androidx.room.*
import com.kc.comiketter2.UserDTO
import com.kc.comiketter2.domain.usecase.SearchUsersUseCase
import twitter4j.User

@Dao
interface AppDao : SearchUsersUseCase {
  @Query("SELECT * FROM users WHERE circle_name LIKE :circleName")
  override suspend fun searchUsersFromCircleName(circleName: String): List<User>?

  @Query("SELECT * FROM users WHERE name LIKE :name")
  override suspend fun searchUsersFromName(name: String): List<User>?

  @Query("SELECT * FROM users WHERE screen_name LIKE :screenName")
  override suspend fun searchUsersFromScreenName(screenName: String): List<User>?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun addUser(user: UserDTO)

  @Update
  suspend fun updateUser(user: UserDTO)
}