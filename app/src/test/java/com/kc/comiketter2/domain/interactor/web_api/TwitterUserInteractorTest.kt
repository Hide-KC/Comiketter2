package com.kc.comiketter2.domain.interactor.web_api

import androidx.test.platform.app.InstrumentationRegistry
import com.kc.comiketter2.BuildConfig
import com.kc.comiketter2.LogDecorator
import com.kc.comiketter2.R
import com.kc.comiketter2.domain.usecase.web_api.TwitterUserUseCase
import com.kc.comiketter2.oauth.TwitterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import twitter4j.Twitter
import twitter4j.auth.AccessToken
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
@Suppress("ConstantConditionIf")
class TwitterUserInteractorTest : CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Default

  private val context = InstrumentationRegistry.getInstrumentation().context
  private lateinit var accessToken: AccessToken
  private lateinit var twitter: Twitter
  private lateinit var useCase: TwitterUserUseCase
  private val userId = 3243831438 // kcpoipoiのユーザID
  private lateinit var latch: CountDownLatch

  @Before
  fun setUp() {
    if (BuildConfig.IS_DEBUG) {
      accessToken = AccessToken(context.getString(R.string.token), context.getString(R.string.token_secret))
      twitter = TwitterUtils.getTwitterForDebug(context, accessToken)
    } else {
      twitter = TwitterUtils.getTwitter(context)
    }
    useCase = TwitterUserInteractor(context, twitter)
  }

  @After
  fun tearDown() {
  }

  @Test
  fun getFollowUsers() {
    latch = CountDownLatch(1)

    launch {
      val users = useCase.getFollowUsers(userId, object : TwitterUserUseCase.OnNotifyProgressListener {
        override suspend fun onNotifyFollowUsersCount(count: Int) {
          if (BuildConfig.IS_DEBUG) {
            println(LogDecorator.decoCyan("onNotifyFollowUsersCount = $count"))
          }
          assertThat(count)
            .isEqualTo(USERS)
        }

        override suspend fun onNotifyProgress(arg: Int) {
          if (BuildConfig.IS_DEBUG) {
            println(LogDecorator.decoGreen("onNotifyProgress = $arg"))
          }
          assertThat(arg)
            .isNotZero()
        }
      })

      assertThat(users)
        .isNotEmpty
        .hasSize(USERS)
      latch.countDown()
    }

    latch.await()
  }

  companion object {
    private const val USERS = 2101
  }
}