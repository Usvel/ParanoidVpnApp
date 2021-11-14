package com.paranoid.vpn.app.profile.ui.login;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.paranoid.vpn.app.R;

public class LoginFragmentDirections {
  private LoginFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionLoginFragmentToProfileFragment() {
    return new ActionOnlyNavDirections(R.id.action_login_fragment_to_profile_fragment);
  }
}
