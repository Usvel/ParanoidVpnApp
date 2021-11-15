package com.paranoid.vpn.app.profile.ui.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.MessageData
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.usecase.GetFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.ReauthenticateFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.UpdateEmailFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.UpdateProfileFirebaseUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import kotlinx.coroutines.launch

class EditUserViewModel : BaseFragmentViewModel() {
    private val _networkStateUser: MutableLiveData<NetworkStatus<UserEntity>> = MutableLiveData()
    val networkStateUser: LiveData<NetworkStatus<UserEntity>> = _networkStateUser

    private val _networkStateReauthenticate: MutableLiveData<NetworkStatus<UserEntity>> =
        MutableLiveData()
    val networkStateReauthenticate: LiveData<NetworkStatus<UserEntity>> =
        _networkStateReauthenticate

    private val _networkStateUpdateUser: MutableLiveData<NetworkStatus<UserEntity>> =
        MutableLiveData()
    val networkStateUpdateUser: LiveData<NetworkStatus<UserEntity>> = _networkStateUpdateUser

    private val _statePasswordUser = MutableLiveData<Boolean>().apply {
        value = false
    }
    val statePasswordUser: MutableLiveData<Boolean> = _statePasswordUser

    private var user: UserEntity? = null

    private val getFirebaseUserUseCase =
        GetFirebaseUserUseCase(UserFirebaseImpl)

    private val updateProfileFirebaseUseCase = UpdateProfileFirebaseUseCase(UserFirebaseImpl)

    private val updateEmailFirebaseUseCase = UpdateEmailFirebaseUserUseCase(UserFirebaseImpl)

    private val reauthenticateFirebaseUseCase = ReauthenticateFirebaseUserUseCase(UserFirebaseImpl)

    override fun getCurrentData() {
        val user = getFirebaseUserUseCase.execute()
        Log.d(TAG, user.toString())
        if (user == null) {
            _networkStateUser.value = NetworkStatus.Error(
                MessageData(
                    Utils.getString(R.string.firebase_error),
                    Utils.getString(
                        R.string.firebase_error_loading_user
                    )
                )
            )
        } else {
            this.user = user
            _networkStateUser.value = NetworkStatus.Success(user)
        }
    }

    fun onProceed(userName: String, imageUrl: String, email: String) {
        viewModelScope.launch {
            try {
                _networkStateUpdateUser.value = NetworkStatus.Loading()
                if ((userName != user?.name) || (imageUrl != user?.photoUrl))
                    updateProfileFirebaseUseCase.execute(userName, imageUrl)
                if (email != user?.email)
                    updateEmailFirebaseUseCase.execute(email)
                _networkStateUpdateUser.value = NetworkStatus.Success()
            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    _statePasswordUser.value = true
                }
                _networkStateUpdateUser.value = NetworkStatus.Error(
                    MessageData(
                        Utils.getString(R.string.firebase_error),
                        e.message
                    )
                )
            }
        }
    }

    fun onReauthenticate(password: String, newEmail: String) {
        viewModelScope.launch {
            try {
                _networkStateReauthenticate.value = NetworkStatus.Loading()
                user?.email?.let { email ->
                    reauthenticateFirebaseUseCase.execute(email, password)
                }
                _networkStateReauthenticate.value = NetworkStatus.Success()
                getCurrentData()
                onProceed(user?.name.toString(), user?.photoUrl.toString(), newEmail)
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                _networkStateReauthenticate.value = NetworkStatus.Error(
                    MessageData(
                        title = Utils.getString(R.string.firebase_error),
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
