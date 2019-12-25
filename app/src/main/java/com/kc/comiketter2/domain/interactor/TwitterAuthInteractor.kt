package com.kc.comiketter2.domain.interactor

import android.content.Context
import com.kc.comiketter2.domain.usecase.TwitterAuthUseCase

class TwitterAuthInteractor(context: Context) : TwitterAuthUseCase {
  override suspend fun getAccessToken(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}