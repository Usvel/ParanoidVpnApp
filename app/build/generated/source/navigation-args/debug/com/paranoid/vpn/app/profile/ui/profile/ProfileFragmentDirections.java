package com.paranoid.vpn.app.profile.ui.profile;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.paranoid.vpn.app.R;

public class ProfileFragmentDirections {
  private ProfileFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionProfileFragmentToRegistrationFragment() {
    return new ActionOnlyNavDirections(R.id.action_profile_fragment_to_registration_fragment);
  }

  @NonNull
  public static NavDirections actionProfileFragmentToLoginFragment() {
    return new ActionOnlyNavDirections(R.id.action_profile_fragment_to_login_fragment);
  }
}
