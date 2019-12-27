package com.kc.comiketter2.oauth

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kc.comiketter2.domain.interactor.TwitterAuthInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import twitter4j.auth.AccessToken
import kotlin.coroutines.CoroutineContext

class OAuthCallbackViewModel(private val app: Application) : AndroidViewModel(app), CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Default + Job()

  private val _nullValueEvent = MutableLiveData<Unit>()
  val nullValueEvent: LiveData<Unit>
    get() = _nullValueEvent
  private val _accessTokenLiveData = MutableLiveData<AccessToken>()
  val accessTokenLiveData: LiveData<AccessToken>
    get() = _accessTokenLiveData
  private val _onStartBrowserEvent = MutableLiveData<Intent>()
  val onStartBrowserEvent: LiveData<Intent>
    get() = _onStartBrowserEvent

  private val authUseCase = TwitterAuthInteractor(app)

  fun startAuthorization() {
    viewModelScope.launch(coroutineContext) {
      val authenticationUrl = authUseCase.getAuthenticationUrl()
      if (authenticationUrl != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authenticationUrl))
        _onStartBrowserEvent.postValue(intent)
      } else {
        Log.e(this@OAuthCallbackViewModel.javaClass.simpleName, "getAuthenticationUrl Failed")
        _nullValueEvent.postValue(Unit)
      }
    }
  }

  fun onOAuthCallback(verifier: String) {
    viewModelScope.launch(coroutineContext) {
      val accessToken = authUseCase.getAccessToken(verifier)
      if (accessToken != null) {
        _accessTokenLiveData.postValue(accessToken)
      } else {
        Log.e(this@OAuthCallbackViewModel.javaClass.simpleName, "getAccessToken Failed")
        _nullValueEvent.postValue(Unit)
      }
    }
  }
}