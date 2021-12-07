package com.paranoid.vpn.app.profile.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.MessageData
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.UserLoggedState
import com.paranoid.vpn.app.common.utils.Utils.getString
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.usecase.DeleteFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.GetFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.ReauthenticateFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.SignOutFirebaseUserUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import com.paranoid.vpn.app.profile.ui.edit.EditUserViewModel
import kotlinx.coroutines.launch

class ProfileViewModel : BaseFragmentViewModel() {
    private val _userState = MutableLiveData<UserLoggedState>().apply {
        value = UserLoggedState.USER_LOGGED_OUT
    }
    val userState: LiveData<UserLoggedState> = _userState

    private val _networkStateReauthenticate: MutableLiveData<NetworkStatus<UserEntity>> =
        MutableLiveData()
    val networkStateReauthenticate: LiveData<NetworkStatus<UserEntity>> =
        _networkStateReauthenticate

    private val _statePasswordUser = MutableLiveData<Boolean>().apply {
        value = false
    }
    val statePasswordUser: MutableLiveData<Boolean> = _statePasswordUser

    private val _networkStateDeleteUser: MutableLiveData<NetworkStatus<UserEntity>> =
        MutableLiveData()
    val networkStateDeleteUser: LiveData<NetworkStatus<UserEntity>> = _networkStateDeleteUser

    private val _user = MutableLiveData<UserEntity>().apply {
        value = UserEntity()
    }
    val user: LiveData<UserEntity> = _user

    private val getFirebaseUserUseCase =
        GetFirebaseUserUseCase(UserFirebaseImpl)

    private val signOutFirebaseUserUseCase =
        SignOutFirebaseUserUseCase(UserFirebaseImpl)

    private val deleteFirebaseUserUseCase = DeleteFirebaseUserUseCase(UserFirebaseImpl)

    private val reauthenticateFirebaseUseCase = ReauthenticateFirebaseUserUseCase(UserFirebaseImpl)

    override fun getCurrentData() {
        try {
            val user = getFirebaseUserUseCase.execute()
            Log.d(TAG, user.toString())
            if (user == null) {
                _user.value = UserEntity(
                    name = getString(R.string.name_user)
                )
                _userState.value = UserLoggedState.USER_LOGGED_OUT
            } else {
                _user.value = user
                _userState.value = UserLoggedState.USER_LOGGED_IN
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
        }
    }

    fun singOutUser() {
        try {
            signOutFirebaseUserUseCase.execute()
            getCurrentData()
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            try {
                _networkStateDeleteUser.value = NetworkStatus.Loading()
                deleteFirebaseUserUseCase.execute()
                _statePasswordUser.value = true
                _networkStateDeleteUser.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    _statePasswordUser.value = true
                }
                _networkStateDeleteUser.value = NetworkStatus.Error(
                    MessageData(
                        title = getString(R.string.firebase_error),
                        message = e.message.toString()
                    )
                )
            }
        }
    }

    fun onReauthenticate(password: String) {
        viewModelScope.launch {
            try {
                _networkStateReauthenticate.value = NetworkStatus.Loading()
                _user.value?.email?.let { email ->
                    reauthenticateFirebaseUseCase.execute(email, password)
                }
                _networkStateReauthenticate.value = NetworkStatus.Success()
                _statePasswordUser.value = false
                deleteUser()
            } catch (e: Exception) {
                Log.e(EditUserViewModel.TAG, e.message.toString())
                _networkStateReauthenticate.value = NetworkStatus.Error(
                    MessageData(
                        title = getString(R.string.firebase_error),
                        message = e.message
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "ProfileViewModel"
    }
}
