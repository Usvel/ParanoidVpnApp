package com.paranoid.vpn.app.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.paranoid.vpn.app.R

// All information here: https://github.com/AppIntro/AppIntro


class ParanoidIntroScreen : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure you don't call setContentView!

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(
            AppIntroFragment.newInstance(
                title = "Paranoid VPN",
                description = "Welcome to the best VPN Client ever",
                imageDrawable = R.drawable.intro_logo
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "Proxy!",
                description = "Use wide list of online proxy or create your own!",
                imageDrawable = R.drawable.intro_router
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "AdBlock!",
                description = "Block annoying advertisement with wide list of IPs or add your own!",
                imageDrawable = R.drawable.intro_adblock
            )
        )

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            )
        )

        // Show/hide status bar
        showStatusBar(true);

        //Speed up or down scrolling
        setScrollDurationFactor(2);

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()
    }
}