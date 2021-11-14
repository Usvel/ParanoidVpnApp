package com.paranoid.vpn.app.profile.ui.registration;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.paranoid.vpn.app.R;

public class RegistrationFragmentDirections {
  private RegistrationFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionRegistrationFragmentToProfileFragment() {
    return new ActionOnlyNavDirections(R.id.action_registration_fragment_to_profile_fragment);
  }
}
