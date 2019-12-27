package com.kc.comiketter2.model.data.room.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kc.comiketter2.model.data.room.UserEntity
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

@RunWith(AndroidJUnit4::class)
class AppLocalDataSourceTest : CoroutineScope {
  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private lateinit var latch: CountDownLatch
  private val localDataSource = AppLocalDataSource.getInstance(AppLocalDatabase.getInstance(context).appDao())

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO

  @Before
  fun setUp() {
    latch = CountDownLatch(1)
    launch {
      withContext(coroutineContext) {
        val users = arrayOf(
          UserEntity(circleName = "ほげほげ", userId = 1, name = "ほげ", screenName = "ふが", profileImageUrl = "https://hoge", profileDescription = "ふー", userUrl = "https://twitter.com/kcpoipoi"),
          UserEntity(circleName = "ふがふが", userId = 2, name = "hoge", screenName = "fuga", profileImageUrl = "https://foo", profileDescription = "ふー", userUrl = "https://twitter.com/kcpoipoi")
        )

        try {
          localDataSource.addUsers(*users)
        } catch (e: Exception) {
          println(e)
        } finally {
          latch.countDown()
        }
      }
    }
    latch.await()
  }

  @After
  fun tearDown() {
  }

  @Test
  fun searchUsersFromScreenName() {
    latch = CountDownLatch(1)

    launch {
      val list = localDataSource.searchUsersFromName("ほげ")
      println(list)
      assertThat(list)
        .isNotNull
        .hasSize(1)
      latch.countDown()
    }
    latch.await()
  }

  @Test
  fun searchUsersFromName() {
  }

  @Test
  fun searchUsersFromCircleName() {
  }

  @Test
  fun addUser() {
  }

  @Test
  fun addUsers() {
  }

  @Test
  fun updateUser() {
  }

  @Test
  fun updateUsers() {
  }

  @Test
  fun deleteUser() {
  }

  @Test
  fun deleteUsers() {
  }

  @Test
  fun deleteAllUsers() {
  }
}