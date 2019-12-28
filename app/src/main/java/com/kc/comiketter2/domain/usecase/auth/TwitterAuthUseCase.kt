package com.kc.comiketter2.domain.usecase.auth

import kotlinx.coroutines.CoroutineScope
import twitter4j.auth.AccessToken

interface TwitterAuthUseCase : CoroutineScope {
  suspend fun getAuthenticationUrl(): String?
  suspend fun getAccessToken(verifier: String): AccessToken?
}