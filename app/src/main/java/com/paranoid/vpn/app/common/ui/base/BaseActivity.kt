package com.paranoid.vpn.app.common.ui.base

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
    protected var navController: NavController? = null

    private var bottomNav: BottomNavigationView? = null

    private var menuMode = MENU_MODE_CLEAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater, null, false)
        setContentView(binding.root)
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        navController = navHostFragment.navController
    }

    protected fun setBottomNav(bottomNav: BottomNavigationView) {
        this.bottomNav = bottomNav
    }

    fun setUpBottomNav() {
        bottomNav?.let {
            if (menuMode != MENU_MODE_START) {
                it.menu.clear()
                //it.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_UNLABELED
                menuInflater.inflate(R.menu.bottom_nav_menu, it.menu)
                navController?.let { navController ->
                    it.setupWithNavController(navController)
                }
                it.setOnItemSelectedListener { item ->
                    Log.d(TAG, item.toString())
                    NavigationUI.onNavDestinationSelected(
                        item,
                        Navigation.findNavController(this, R.id.my_nav_host_fragment)
                    )
                }
                menuMode = MENU_MODE_START
            }
        }
    }

    override fun onDestroy() {
        navController = null
        bottomNav = null
        super.onDestroy()
    }

    fun setProceedBottomNav(event: () -> Unit) {
        bottomNav?.let {
            if (menuMode != MENU_MODE_NEXT) {
                it.menu.clear()
                //it.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
                menuInflater.inflate(R.menu.bottom_nav_proceed, it.menu)

                it.setOnItemSelectedListener {
                    when (it.itemId) {
                        R.id.proceed -> {
                            event()
                            true
                        }
                        else -> false
                    }
                }
                menuMode = MENU_MODE_NEXT
            }
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

    companion object {
        const val TAG = "BaseActivity"
        private const val MENU_MODE_CLEAR = -1
        private const val MENU_MODE_START = 0
        private const val MENU_MODE_NEXT = 1
    }
}
