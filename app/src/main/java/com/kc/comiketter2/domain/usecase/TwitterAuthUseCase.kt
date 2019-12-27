package com.kc.comiketter2.domain.usecase

import kotlinx.coroutines.CoroutineScope
import twitter4j.auth.AccessToken

interface TwitterAuthUseCase : CoroutineScope {
  suspend fun getAuthenticationUrl(): String?
  suspend fun getAccessToken(verifier: String): AccessToken?
}