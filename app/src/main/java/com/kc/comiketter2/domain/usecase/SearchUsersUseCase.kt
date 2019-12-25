package com.kc.comiketter2.domain.usecase

import twitter4j.User

interface SearchUsersUseCase {
  suspend fun searchUsersFromScreenName(screenName: String): List<User>?
  suspend fun searchUsersFromName(name: String): List<User>?
  suspend fun searchUsersFromCircleName(circleName: String): List<User>?
}