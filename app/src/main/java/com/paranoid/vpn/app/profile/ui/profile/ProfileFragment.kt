package com.paranoid.vpn.app.profile.ui.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.Application
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.ui.factory.DaggerViewModelFactory
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.UserLoggedState
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.isValidEmail
import com.paranoid.vpn.app.databinding.NavigationProfileFragmentBinding
import javax.inject.Inject

class ProfileFragment :
    BaseFragment<NavigationProfileFragmentBinding, ProfileViewModel>(
        NavigationProfileFragmentBinding::inflate
    ) {
    @Inject
    lateinit var viewModelFactory: DaggerViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDagger()
    }

    private fun initGraph() {
        val chart: BarChart = binding.bcGraph

        val noOfEmp = ArrayList<BarEntry>()

        noOfEmp.add(BarEntry(1040f, 0f))
        noOfEmp.add(BarEntry(1040f, 1f))
        noOfEmp.add(BarEntry(1133f, 2f))
        noOfEmp.add(BarEntry(1240f, 3f))
        noOfEmp.add(BarEntry(1369f, 4f))
        noOfEmp.add(BarEntry(1487f, 5f))
        noOfEmp.add(BarEntry(1501f, 6f))
        noOfEmp.add(BarEntry(1645f, 7f))
        noOfEmp.add(BarEntry(1578f, 8f))
        noOfEmp.add(BarEntry(1695f, 9f))


        val year = ArrayList<Any>()

        year.add("2008")
        year.add("2009")
        year.add("2010")
        year.add("2011")
        year.add("2012")
        year.add("2013")
        year.add("2014")
        year.add("2015")
        year.add("2016")
        year.add("2017")

        val bardataset = BarDataSet(noOfEmp, "No Of Employee")
        chart.animateY(5000)
        val data = BarData(bardataset)
        bardataset.setColors(*ColorTemplate.COLORFUL_COLORS)
        chart.data = data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setListeners()
        initGraph()
    }

    override fun onStop() {
        setUpBottomNav()
        super.onStop()
    }

    private fun setListeners() {
        binding.cvProfileRegistration.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_registration_fragment)
        }
        binding.cvProfileLogin.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_login_fragment)
        }
        binding.cvProfileBasket.setOnClickListener {
            viewModel?.deleteUser()
        }
        binding.viewChangeProfile.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_edit_user_fragment)
        }
        binding.cvProfileOut.setOnClickListener {
            viewModel?.singOutUser()
        }
    }

    private fun setObservers() {
        viewModel?.userState?.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    UserLoggedState.USER_LOGGED_IN -> {
                        binding.cvGraph.visibility = View.VISIBLE
                        binding.viewChangeProfile.visibility = View.VISIBLE
                        binding.cvProfileBasket.visibility = View.VISIBLE
                        binding.tvProfileStatistics.visibility = View.VISIBLE
                        binding.cvProfileOut.visibility = View.VISIBLE

                        binding.tvProfileEmail.visibility = View.VISIBLE
                        binding.tvProfileEmailProfile.visibility = View.VISIBLE

                        binding.cvProfileLogin.visibility = View.GONE
                        binding.cvProfileRegistration.visibility = View.GONE
                    }
                    UserLoggedState.USER_LOGGED_OUT -> {
                        binding.cvGraph.visibility = View.GONE
                        binding.viewChangeProfile.visibility = View.GONE
                        binding.cvProfileBasket.visibility = View.GONE
                        binding.cvProfileOut.visibility = View.GONE
                        binding.tvProfileStatistics.visibility = View.GONE

                        binding.tvProfileEmail.visibility = View.GONE
                        binding.tvProfileEmailProfile.visibility = View.GONE

                        binding.cvProfileLogin.visibility = View.VISIBLE
                        binding.cvProfileRegistration.visibility = View.VISIBLE
                        binding.tilProfilePassword.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewModel?.user?.observe(viewLifecycleOwner) {
            if (it.name.isNullOrEmpty()) {
                binding.tvProfileName.text = Utils.getString(R.string.name_user)
            } else {
                binding.tvProfileName.text = it.name
            }
            if (it.email.isNullOrEmpty()) {
                binding.tvProfileEmail.visibility = View.GONE
                binding.tvProfileEmailProfile.visibility = View.GONE
            } else {
                binding.tvProfileEmailProfile.text = it.email
            }
            if (it.photoUrl.isNullOrEmpty()) {
                binding.ivProfileImage.setImageResource(R.drawable.ic_image_profile)
            } else {
                Glide.with(this).load(it.photoUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.ivProfileImage.setImageResource(R.drawable.ic_image_profile)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .apply(RequestOptions.circleCropTransform()).into(binding.ivProfileImage)
            }
        }
        viewModel?.networkStateDeleteUser?.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Error -> {
                    setProgressVisibility(false)
                    it.messageData?.let { messageData -> showMessage(messageData) }
                }
                is NetworkStatus.Loading -> {
                    setProgressVisibility(true)
                }
                is NetworkStatus.Success -> {
                    setProgressVisibility(false)
                    setUpBottomNav()
                    viewModel?.getCurrentData()
                }
            }
        }
        viewModel?.statePasswordUser?.observe(viewLifecycleOwner) {
            if (it) {

                binding.cvGraph.visibility = View.GONE
                binding.viewChangeProfile.visibility = View.GONE
                binding.cvProfileBasket.visibility = View.GONE
                binding.cvProfileOut.visibility = View.GONE
                binding.tvProfileStatistics.visibility = View.GONE

                binding.tvProfileEmail.visibility = View.VISIBLE
                binding.tvProfileEmailProfile.visibility = View.VISIBLE

                binding.cvProfileLogin.visibility = View.VISIBLE
                binding.cvProfileRegistration.visibility = View.VISIBLE
                binding.tilProfilePassword.visibility = View.GONE

                setProceedBottomNav {
                    if (binding.etProfilePassword.text.isValidEmail()) {
                        binding.tilProfilePassword.error =
                            Utils.getString(R.string.edit_user_fragment_invalid_email)
                        return@setProceedBottomNav
                    }

                    viewModel?.onReauthenticate(binding.etProfilePassword.text.toString())
                }
            }
        }
        viewModel?.networkStateReauthenticate?.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Error -> {
                    setProgressVisibility(false)
                    it.messageData?.let { messageData -> showMessage(messageData) }
                    viewModel?.getCurrentData()
                }
                is NetworkStatus.Loading -> {
                    setProgressVisibility(true)
                }
                is NetworkStatus.Success -> {
                    setProgressVisibility(false)
                    viewModel?.getCurrentData()
                }
            }
        }
    }

    private fun initDagger() {
        (requireActivity().application as Application).getAppComponent()
            .registerProfileFeatureComponent()
            .create().registerProfileComponent().create().inject(this)
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
    }

    companion object {
        const val TAG = "ProfileFragment"
    }
}
