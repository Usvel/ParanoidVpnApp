package com.paranoid.vpn.app.common.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.paranoid.vpn.app.common.ui.app.AppActivity
import com.paranoid.vpn.app.common.ui.factory.DaggerViewModelFactory
import javax.inject.Inject

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding, VM : BaseFragmentViewModel>(
    private val inflate: Inflate<VB>
) : Fragment() {



    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private var baseActivity: BaseActivity<*>? = null

    protected var viewModel: VM? = null

    private var appActivity: AppActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (context is AppActivity) {
            appActivity = context as AppActivity
        }
        initViewModel()
        baseActivity = context as? BaseActivity<*>
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    override fun onDestroyView() {
        _binding = null
        appActivity = null
        baseActivity = null
        super.onDestroyView()
    }

    abstract fun initViewModel()

    private fun initData() {
        viewModel?.getCurrentData()
    }

    fun setUpBottomNav() {
        appActivity?.setUpBottomNav()
    }

    fun setProceedBottomNav(next: () -> Unit) {
        appActivity?.setProceedBottomNav(next)
    }

    fun setProgressVisibility(isVisible: Boolean) {
        baseActivity?.setProgressVisibility(isVisible)
    }

    fun showMessage(
        messageData: MessageData
    ) {
        baseActivity?.showMessage(messageData)
    }
}
