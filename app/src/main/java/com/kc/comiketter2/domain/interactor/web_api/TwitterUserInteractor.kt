package com.kc.comiketter2.domain.interactor.web_api

import android.content.Context
import com.kc.comiketter2.BuildConfig
import com.kc.comiketter2.domain.usecase.web_api.TwitterUserUseCase
import com.kc.comiketter2.model.data.room.UserEntity
import twitter4j.Twitter
import java.text.Normalizer

class TwitterUserInteractor(private val context: Context, private val twitter: Twitter) : TwitterUserUseCase {

  override suspend fun getFollowUsers(userId: Long, listener: TwitterUserUseCase.OnNotifyProgressListener?): List<UserEntity> {
    val cursor = -1L
    val idsList = getFriendsIDs(userId, cursor, mutableListOf())
    listener?.onNotifyFollowUsersCount(idsList.size)

    if (idsList.isEmpty()) {
      return listOf()
    }
    return getUsers(idsList, 0, mutableListOf(), listener)
  }

  private suspend fun getFriendsIDs(userId: Long, cursor: Long, idsList: MutableList<Long>): List<Long> {
    val ids = twitter.getFriendsIDs(userId, cursor)
    if (ids != null) {
      idsList.addAll(ids.iDs.toList())
      val rateLimit = ids.rateLimitStatus
      return when {
        // レートリミットに達したらその時点で返す
        rateLimit.remaining <= 0 -> {
          idsList
        }
        // cursorをシフトして再帰的に呼び出し
        ids.hasNext() -> {
          getFriendsIDs(userId, ids.nextCursor, idsList)
        }
        // ids.hasNext == false で返す
        else -> {
          idsList
        }
      }
    } else {
      return idsList
    }
  }

  @Suppress("ConstantConditionIf")
  private suspend fun getUsers(ids: List<Long>, startIndex: Int, users: MutableList<UserEntity>, listener: TwitterUserUseCase.OnNotifyProgressListener?): List<UserEntity> {
    val endIndex = if (startIndex + 99 > ids.lastIndex) {
      ids.lastIndex
    } else {
      startIndex + 99
    }

    val idsSlice = ids.slice(startIndex..endIndex)
    val userResponseList = twitter.lookupUsers(*idsSlice.toLongArray())
    val userEntities = userResponseList
      .map { user ->
        val name = Normalizer.normalize(user.name, Normalizer.Form.NFKC)
        if (BuildConfig.IS_DEBUG) {
          println(name)
        }
        UserEntity(
          0,
          user.id,
          name,
          user.screenName,
          user.profileImageURL,
          user.description,
          "")
      }

    users.addAll(userEntities)
    listener?.onNotifyProgress(users.size)

    val rateLimit = userResponseList.rateLimitStatus

    return when {
      rateLimit.remaining <= 0 || endIndex == ids.lastIndex -> {
        users
      }
      else -> {
        getUsers(ids, endIndex + 1, users, listener)
      }
    }
  }
}