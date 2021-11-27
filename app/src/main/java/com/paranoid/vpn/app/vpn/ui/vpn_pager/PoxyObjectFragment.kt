package com.paranoid.vpn.app.vpn.ui.vpn_pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.paranoid.vpn.app.R

class PoxyObjectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.page_proxy_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}