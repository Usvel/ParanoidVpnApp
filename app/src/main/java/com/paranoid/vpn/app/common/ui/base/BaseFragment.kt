package com.paranoid.vpn.app.common.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private var baseActivity: BaseActivity<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = context as? BaseActivity<*>
        initBottomNav()
        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        baseActivity = null
        super.onDestroyView()
    }

    abstract fun initViewModel()

    abstract fun initBottomNav()

    protected fun setProceedBottomNav(event: () -> Unit) {
        baseActivity?.setProceedBottomNav(event)
    }

    protected fun setUpBottomNav() {
        baseActivity?.setUpBottomNav()
    }
}
