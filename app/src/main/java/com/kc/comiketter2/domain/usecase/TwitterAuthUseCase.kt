package com.kc.comiketter2.domain.usecase

interface TwitterAuthUseCase {
  suspend fun getAccessToken(): String
}