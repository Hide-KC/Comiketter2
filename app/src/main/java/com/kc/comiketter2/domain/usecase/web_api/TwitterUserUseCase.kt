package com.kc.comiketter2.domain.usecase.web_api

import com.kc.comiketter2.model.data.room.UserEntity

interface TwitterUserUseCase {
  suspend fun getFollowUsers(userId: Long, listener: OnNotifyProgressListener? = null): List<UserEntity>
  interface OnNotifyProgressListener {
    suspend fun onNotifyFollowUsersCount(count: Int)
    suspend fun onNotifyProgress(arg: Int)
  }
}