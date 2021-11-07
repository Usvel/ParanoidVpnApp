package com.paranoid.vpn.app.profile.ui.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.profile.domain.usecase.CreateFirebaseUserUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import com.paranoid.vpn.app.profile.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel : BaseFragmentViewModel() {
    private val _networkState: MutableLiveData<NetworkStatus> = MutableLiveData()
    val networkState: LiveData<NetworkStatus> = _networkState

    private val createFirebaseUserUseCase =
        CreateFirebaseUserUseCase(UserFirebaseImpl)

    override fun getCurrentData() {
        // Загружать ничего(пока)
    }

    fun createUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _networkState.value = NetworkStatus.Loading()
                withContext(Dispatchers.IO) {
                    createFirebaseUserUseCase.execute(email, password)
                }
                _networkState.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(LoginViewModel.TAG, e.message.toString())
                _networkState.value = NetworkStatus.Error(e.message)
            }
        }
    }

    companion object {
        const val TAG = "RegistrationViewModel"
    }
}
