package com.kc.comiketter2.domain.interactor

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
class TwitterAuthInteractorTest : CoroutineScope {
  private val context = InstrumentationRegistry.getInstrumentation().context
  private val authUseCase = TwitterAuthInteractor(context)
  private lateinit var latch: CountDownLatch

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Default

  @Before
  fun setUp() {
  }

  @After
  fun tearDown() {
  }

  @Test
  fun getAuthenticationUrl() {
    latch = CountDownLatch(1)
    launch {
      val authenticationUrl = authUseCase.getAuthenticationUrl()
      println(authenticationUrl)
      assertThat(authenticationUrl)
        .isNotNull()
        .hasSizeGreaterThan(1)
        .contains("oauth_token=")
      latch.countDown()
    }
    latch.await()
  }

  @Test
  fun getAccessToken() {

  }
}