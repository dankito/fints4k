package net.dankito.banking.ui.android.alerts

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import net.dankito.banking.ui.android.R


open class AskDismissChangesAlert {

    open fun show(dialog: DialogFragment) {
        val context = dialog.requireContext()

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.alert_ask_discard_changes_title))
            .setMessage(context.getString(R.string.alert_ask_discard_changes_message))
            .setPositiveButton(R.string.discard) { alert, _ ->
                alert.dismiss()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { alert, _ -> alert.dismiss() }
            .show()
    }

}