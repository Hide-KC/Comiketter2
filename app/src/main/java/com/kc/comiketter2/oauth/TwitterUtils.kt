package com.kc.comiketter2.oauth

import android.content.Context
import android.preference.PreferenceManager
import com.kc.comiketter2.R
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

object TwitterUtils {
  private const val TOKEN = "token"
  private const val TOKEN_SECRET = "token_secret"

  fun getTwitter(context: Context): Twitter {
    val twitter = TwitterFactory().instance
    twitter.setOAuthConsumer(context.getString(R.string.consumer_key), context.getString(R.string.consumer_secret))
    loadAccessToken(context)?.let {token ->
      twitter.oAuthAccessToken = token
    }
    return twitter
  }

  private fun loadAccessToken(context: Context): AccessToken? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val token = prefs.getString(TOKEN, null)
    val tokenSecret = prefs.getString(TOKEN_SECRET, null)
    return if (token != null && tokenSecret != null) {
      AccessToken(token, tokenSecret)
    } else {
      null
    }
  }

  fun storeAccessToken(context: Context, accessToken: AccessToken) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    prefs.edit().let {
      it.putString(TOKEN, accessToken.token)
      it.putString(TOKEN_SECRET, accessToken.tokenSecret)
      it.apply()
    }
  }
}