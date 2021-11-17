package com.paranoid.vpn.app.common.remote

import com.google.android.gms.auth.api.signin.GoogleSignInClient

interface GoogleService {
    fun getGoogleSignInClient(): GoogleSignInClient
}