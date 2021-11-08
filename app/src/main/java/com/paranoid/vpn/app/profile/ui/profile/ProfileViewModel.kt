package com.paranoid.vpn.app.profile.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.utils.UserLoggedState
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.usecase.GetFirebaseUserUseCase
import com.paranoid.vpn.app.profile.domain.usecase.SignOutFirebaseUserUseCase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl

class ProfileViewModel : BaseFragmentViewModel() {
    private val _userState = MutableLiveData<UserLoggedState>().apply {
        value = UserLoggedState.USER_LOGGED_OUT
    }
    val userState: LiveData<UserLoggedState> = _userState

    private val _user = MutableLiveData<UserEntity>().apply {
        value = UserEntity()
    }
    val user: LiveData<UserEntity> = _user

    private val getFirebaseUserUseCase =
        GetFirebaseUserUseCase(UserFirebaseImpl)

    private val signOutFirebaseUserUseCase =
        SignOutFirebaseUserUseCase(UserFirebaseImpl)

    override fun getCurrentData() {
        try {
            val user = getFirebaseUserUseCase.execute()
            Log.d(TAG, user.toString())
            if (user == null) {
                _user.value = UserEntity(
                    name = Utils.getString(R.string.name_user)
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

    companion object {
        const val TAG = "ProfileViewModel"
    }
}
