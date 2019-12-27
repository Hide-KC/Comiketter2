package com.kc.comiketter2.domain.interactor

import android.content.Context
import com.kc.comiketter2.domain.usecase.TwitterAuthUseCase

class TwitterAuthInteractor(private val context: Context) : TwitterAuthUseCase {
  override suspend fun getAccessTokenAndSecret(): Array<String> {
    return arrayOf("hoge", "fuga")
  }
}