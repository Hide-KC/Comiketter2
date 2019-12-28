package com.kc.comiketter2.domain.interactor.auth

import android.content.Context
import com.kc.comiketter2.R
import com.kc.comiketter2.domain.usecase.auth.TwitterAuthUseCase
import com.kc.comiketter2.oauth.TwitterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import twitter4j.TwitterException
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import kotlin.coroutines.CoroutineContext

class TwitterAuthInteractor(private val context: Context) : TwitterAuthUseCase {
  private val twitter = TwitterUtils.getTwitter(context)
  private lateinit var requestToken: RequestToken

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Default + Job()

  override suspend fun getAuthenticationUrl(): String? {
    val callbackUrl = context.getString(R.string.callback_url)
    return try {
      val requestToken = twitter.getOAuthRequestToken(callbackUrl)
      this.requestToken = requestToken
      requestToken.authenticationURL
    } catch (e: IllegalStateException) {
      e.printStackTrace()
      null
    } catch (e: TwitterException) {
      e.printStackTrace()
      null
    }
  }

  override suspend fun getAccessToken(verifier: String): AccessToken? {
    return try {
      twitter.getOAuthAccessToken(requestToken, verifier)
    } catch (e: TwitterException) {
      e.printStackTrace()
      null
    }
  }
}