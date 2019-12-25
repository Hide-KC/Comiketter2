package com.kc.comiketter2

import com.kc.comiketter2.domain.interactor.TwitterAuthInteractor
import com.kc.comiketter2.domain.usecase.TwitterAuthUseCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TwitterAuthTest {
  private val context = RuntimeEnvironment.systemContext
  private val authUseCase: TwitterAuthUseCase = TwitterAuthInteractor(context)

  @Test
  fun getAccessToken() {

  }
}