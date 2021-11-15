package com.paranoid.vpn.app.profile.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.util.Util
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.MessageData
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.usecase.LoginFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.ResetPasswordUserUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : BaseFragmentViewModel() {
    private val _networkStateUser: MutableLiveData<NetworkStatus<UserEntity>> = MutableLiveData()

    val networkStateUser: LiveData<NetworkStatus<UserEntity>> = _networkStateUser

    private val _networkStateResetPasswordUser: MutableLiveData<NetworkStatus<MessageData>> =
        MutableLiveData()
    val networkStateResetPasswordUser: LiveData<NetworkStatus<MessageData>> =
        _networkStateResetPasswordUser

    private val loginFirebaseUserUseCase =
        LoginFirebaseUserUseCase(UserFirebaseImpl)

    private val resetPasswordUserUseCase = ResetPasswordUserUseCase(UserFirebaseImpl)

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _networkStateUser.value = NetworkStatus.Loading()
                withContext(Dispatchers.IO) {
                    loginFirebaseUserUseCase.execute(email, password)
                }
                _networkStateUser.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                _networkStateUser.value = NetworkStatus.Error(
                    MessageData(
                        Utils.getString(R.string.firebase_error),
                        e.message
                    )
                )
            }
        }
    }

    fun onResetPassword(email: String) {
        viewModelScope.launch {
            try {
                _networkStateResetPasswordUser.value = NetworkStatus.Loading()
                resetPasswordUserUseCase.execute(email)
                _networkStateResetPasswordUser.value = NetworkStatus.Success(
                    MessageData(
                        Utils.getString(R.string.firebase),
                        Utils.getString(R.string.firebase_reset_password)
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                _networkStateResetPasswordUser.value = NetworkStatus.Error(
                    MessageData(
                        Utils.getString(R.string.firebase_error),
                        e.message
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "LoginViewModel"
    }
}
