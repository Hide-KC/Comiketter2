package com.kc.comiketter2

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import twitter4j.User

@RunWith(RobolectricTestRunner::class)
class SearchUsersTest {
  private val context = RuntimeEnvironment.systemContext
  private lateinit var usersTestData: List<User>

}