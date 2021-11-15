package com.paranoid.vpn.app.profile.ui.registration

import android.app.usage.NetworkStats
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.MessageData
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.usecase.CreateFirebaseUserUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import com.paranoid.vpn.app.profile.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel : BaseFragmentViewModel() {
    private val _networkState: MutableLiveData<NetworkStatus<UserEntity>> = MutableLiveData()
    val networkState: LiveData<NetworkStatus<UserEntity>> = _networkState

    private val createFirebaseUserUseCase =
        CreateFirebaseUserUseCase(UserFirebaseImpl)

    fun createUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _networkState.value = NetworkStatus.Loading()
                withContext(Dispatchers.IO) {
                    createFirebaseUserUseCase.execute(email, password)
                }
                _networkState.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                _networkState.value = NetworkStatus.Error(
                    MessageData(
                        title = Utils.getString(R.string.firebase_error),
                        message = e.message
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "RegistrationViewModel"
    }
}
