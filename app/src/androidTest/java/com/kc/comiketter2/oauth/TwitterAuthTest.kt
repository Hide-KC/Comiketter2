package com.kc.comiketter2.oauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kc.comiketter2.domain.interactor.TwitterAuthInteractor
import com.kc.comiketter2.domain.usecase.TwitterAuthUseCase
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

@RunWith(AndroidJUnit4::class)
class TwitterAuthTest : CoroutineScope {
  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private val authUseCase: TwitterAuthUseCase = TwitterAuthInteractor(context)
  private lateinit var latch: CountDownLatch

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + Job()

  @Test
  fun getAccessToken() {
    latch = CountDownLatch(1)

    launch {
      withContext(coroutineContext) {
        val tokenAndSecret = authUseCase.getAccessTokenAndSecret()
        assertThat(tokenAndSecret)
          .isNotEmpty
          .hasSize(2)
          .containsSequence("hoge", "fuga")
        latch.countDown()
      }
    }
    latch.await()
  }
}