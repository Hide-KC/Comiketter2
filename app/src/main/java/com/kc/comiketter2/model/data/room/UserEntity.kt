package com.kc.comiketter2.model.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *
 */
@Entity(tableName = "users", indices = [Index(value = ["user_id"], unique = true)])
data class UserEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "user_id")
  val userId: Long,

  val name: String,

  @ColumnInfo(name = "screen_name")
  val screenName: String,

  @ColumnInfo(name = "profile_image_url")
  val profileImageUrl: String,

  @ColumnInfo(name = "profile_description")
  val profileDescription: String,

  @ColumnInfo(name = "user_url")
  val userUrl: String,

  @ColumnInfo(name = "circle_name")
  val circleName: String = ""
)