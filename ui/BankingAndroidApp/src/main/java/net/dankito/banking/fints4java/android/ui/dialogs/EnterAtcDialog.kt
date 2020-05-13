package net.dankito.banking.fints4java.android.ui.dialogs

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_atc.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.TanMedium


open class EnterAtcDialog : DialogFragment() {

    companion object {
        const val DialogTag = "EnterAtcDialog"
    }


    protected lateinit var tanMedium: TanMedium

    protected lateinit var atcEnteredCallback: (EnterTanGeneratorAtcResult) -> Unit


    open fun show(tanMedium: TanMedium, activity: AppCompatActivity,
                  fullscreen: Boolean = false, atcEnteredCallback: (EnterTanGeneratorAtcResult?) -> Unit) {

        this.tanMedium = tanMedium
        this.atcEnteredCallback = atcEnteredCallback

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_enter_atc, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        val explanationHtml = rootView.context.getString(R.string.dialog_enter_atc_explanation, tanMedium.displayName)
        rootView.txtAtcExplanationToShowToUser.text = Html.fromHtml(explanationHtml, Html.FROM_HTML_MODE_LEGACY)

        rootView.btnCancel.setOnClickListener { enteringAtcDone(null, null) }

        rootView.btnEnteringAtcDone.setOnClickListener { enteringAtcDone(rootView.edtxtEnteredTan.text.toString(), rootView.edtxtEnteredAtc.text.toString()) }
    }


    protected open fun enteringAtcDone(enteredTan: String?, enteredAtcString: String?) {
        var enteredAtc: Int? = null

        if (enteredAtcString != null) {
            try {
                enteredAtc = enteredAtcString.toInt()
            } catch (e: Exception) {
                showEnteredAtcIsNotANumberError(enteredAtcString)

                return
            }
        }

        val result = if (enteredTan == null || enteredAtc == null) EnterTanGeneratorAtcResult.userDidNotEnterAtc()
                     else EnterTanGeneratorAtcResult.userEnteredAtc(enteredTan, enteredAtc)

        atcEnteredCallback(result)

        dismiss()
    }

    protected open fun showEnteredAtcIsNotANumberError(enteredAtcString: String) {
        activity?.let { context ->
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_atc_error_entered_atc_is_not_a_number, enteredAtcString))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

}