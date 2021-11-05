package com.paranoid.vpn.app.profile.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.NavigationProfileFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.paranoid.vpn.app.common.utils.Utils

class ProfileFragment :
    BaseFragment<NavigationProfileFragmentBinding>(NavigationProfileFragmentBinding::inflate) {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser == null) {
            context?.let {
                Utils.makeToast(it, "User no sing")
            }
        } else {
            context?.let {
                Utils.makeToast(it, "User sing")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.testTextView.text = getString(R.string.this_is_profile)
//        binding.testTextView.setOnClickListener {
//            createAccount("rodion.hairof229@yandex.ru","qwerty")
//        }
        binding.cvProfileImage.setOnClickListener{
            setProceedBottomNav{}
        }
        setUpBottomNav()
    }

    override fun initViewModel() {
        // TODO
    }

    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        activity?.let { fragmentActivity ->
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(fragmentActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        context?.let { Utils.makeToast(it, "createUserWithEmail:success") }
                        val user = mAuth?.currentUser
                        context?.let {
                            Utils.makeToast(it, "${user?.email}")
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        context?.let {
                            Utils.makeToast(it, "Authentication failed.")
                        }
                        Log.d(TAG, task.exception?.localizedMessage.toString())
                        //updateUI(null)
                    }
                }
        }

        // [END create_user_with_email]
    }

    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun initBottomNav() {
        setUpBottomNav()
    }
}
