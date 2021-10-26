package com.paranoid.vpn.app.common.ui.base

import android.app.AlertDialog
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

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: Inflate<VB>
) :
    AppCompatActivity() {
    protected lateinit var binding: VB
    protected lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater, null, false)
        setContentView(binding.root)

        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        navController = navHostFragment.navController
    }

    protected fun setUpBottomNav(navController: NavController, bottomNav: BottomNavigationView) {
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(
                item,
                Navigation.findNavController(this, R.id.my_nav_host_fragment)
            )
        }
    }

    protected fun showMessage(
        title: String?,
        message: String?,
        posBtnTxt: String?,
        negBtnTxt: String?,
        posBtnAction: (() -> Unit)?,
        negBtnAction: (() -> Unit)?,
        cancellable: Boolean
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
}
