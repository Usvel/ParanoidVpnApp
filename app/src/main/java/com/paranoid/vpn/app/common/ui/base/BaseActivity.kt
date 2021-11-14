package com.paranoid.vpn.app.common.ui.base

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.component.ProgressDialog

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: Inflate<VB>
) :
    AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    protected lateinit var binding: VB
    private var navController: NavController? = null

    private var bottomNav: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = bindingInflater.invoke(layoutInflater, null, false)
        setContentView(binding.root)
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        navController = navHostFragment.navController
    }

    fun setUpBottomNav(bottomNav: BottomNavigationView) {
        this.bottomNav = bottomNav
        navController?.let { navController ->
            bottomNav.setupWithNavController(navController)
        }
        bottomNav.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(
                item,
                Navigation.findNavController(this, R.id.my_nav_host_fragment)
            )
        }
    }

    override fun onDestroy() {
        navController = null
        bottomNav = null
        super.onDestroy()
    }

    fun showMessage(
        title: String?,
        message: String?,
        posBtnTxt: String? = null,
        negBtnTxt: String? = null,
        posBtnAction: (() -> Unit)? = null,
        negBtnAction: (() -> Unit)? = null,
        cancellable: Boolean = true
    ) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(cancellable)
            setPositiveButton(posBtnTxt) { _, _ ->
                if (posBtnAction != null) posBtnAction()
            }
            negBtnTxt?.let {
                setNegativeButton(negBtnTxt) { _, _ ->
                    if (negBtnAction != null) negBtnAction()
                }
            }
            show()
        }
    }

    fun setProgressVisibility(isVisible: Boolean) {
        if (isVisible) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog()
            } else {
                progressDialog?.dismissAllowingStateLoss()
            }
            progressDialog?.show(supportFragmentManager, ProgressDialog.TAG)
        } else {
            progressDialog?.dismissAllowingStateLoss()
            progressDialog = null
        }
    }

    companion object {
        const val TAG = "BaseActivity"
    }
}
