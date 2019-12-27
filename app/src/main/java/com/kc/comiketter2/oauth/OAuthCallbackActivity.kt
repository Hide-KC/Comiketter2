package com.kc.comiketter2.oauth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.kc.comiketter2.R
import kotlinx.android.synthetic.main.activity_oauth_callback.*

class OAuthCallbackActivity : AppCompatActivity() {
  private lateinit var viewModel: OAuthCallbackViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_oauth_callback)

    viewModel = ViewModelProviders.of(this).get(OAuthCallbackViewModel::class.java)
    viewModel.accessTokenLiveData.observe(this, Observer { accessToken ->
      TwitterUtils.storeAccessToken(this, accessToken)
      this.finish()
    })
    viewModel.nullValueEvent.observe(this, Observer {
      Toast.makeText(this, "Twitterとの連携に失敗しました", Toast.LENGTH_SHORT).show()
      this.finish()
    })
    viewModel.onStartBrowserEvent.observe(this, Observer { intent ->
      if (intent != null) {
        startActivity(intent)
      }
    })
    viewModel.startAuthorization()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    val callbackUrl = intent?.data
    if (callbackUrl != null && callbackUrl.toString().startsWith(getString(R.string.callback_url))) {
      callbackUrl.getQueryParameter("oauth_verifier")?.let { verifier ->
        viewModel.onOAuthCallback(verifier)
      }
    }
  }
}
