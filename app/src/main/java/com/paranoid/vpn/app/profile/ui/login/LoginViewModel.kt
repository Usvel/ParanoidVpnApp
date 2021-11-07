package com.paranoid.vpn.app.profile.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.profile.domain.usecase.LoginFirebaseUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : BaseFragmentViewModel() {
    private val _networkState: MutableLiveData<NetworkStatus> = MutableLiveData()

    val networkState: LiveData<NetworkStatus> = _networkState

    private val loginFirebaseUserUseCase =
        LoginFirebaseUseCase(UserFirebaseImpl)

    override fun getCurrentData() {
        // Загружать ничего(пока)
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _networkState.value = NetworkStatus.Loading()
                withContext(Dispatchers.IO) {
                    loginFirebaseUserUseCase.execute(email, password)
                }
                _networkState.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                _networkState.value = NetworkStatus.Error(e.message)
            }
        }
    }

    companion object {
        const val TAG = "LoginViewModel"
    }
}
