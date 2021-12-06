package com.paranoid.vpn.app.common.ui.base.component

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paranoid.vpn.app.R

class ProgressDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(
            R.layout.component_progress_dialog,
            null
        )

        val dialog =
            MaterialAlertDialogBuilder(ContextThemeWrapper(activity, R.style.AlertDialogTheme))
                .setView(view)
                .setCancelable(false)
                .create()

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    companion object {
        const val TAG = "ProgressDialog"
    }
}
