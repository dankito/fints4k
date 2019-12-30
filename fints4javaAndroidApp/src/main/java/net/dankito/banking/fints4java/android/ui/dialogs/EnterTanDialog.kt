package net.dankito.banking.fints4java.android.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_enter_tan.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.adapter.TanMediumAdapter
import net.dankito.banking.fints4java.android.ui.listener.ListItemSelectedListener
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.TanMedium
import net.dankito.banking.ui.model.TanMediumStatus
import net.dankito.fints.model.TanChallenge
import net.dankito.fints.model.TanProcedureType
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.tan.FlickercodeDecoder


open class EnterTanDialog : DialogFragment() {

    companion object {
        const val DialogTag = "EnterTanDialog"
    }


    protected lateinit var account: Account

    protected lateinit var tanChallenge: TanChallenge

    protected lateinit var presenter: MainWindowPresenter

    protected lateinit var tanEnteredCallback: (String?) -> Unit

    protected val tanMediumAdapter = TanMediumAdapter()


    open fun show(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter, activity: AppCompatActivity,
                  fullscreen: Boolean = false, tanEnteredCallback: (String?) -> Unit) {

        this.account = account
        this.tanChallenge = tanChallenge
        this.presenter = presenter
        this.tanEnteredCallback = tanEnteredCallback

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.Dialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_enter_tan, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        val flickerCodeView = rootView.flickerCodeView

        if (tanChallenge.tanProcedure.type == TanProcedureType.ChipTanOptisch) {
            if (account.tanMedia.isNotEmpty()) {
                setupSelectTanMediumView(rootView)
            }

            flickerCodeView.visibility = View.VISIBLE
            flickerCodeView.setCode(FlickercodeDecoder().decodeChallenge(tanChallenge.tanChallenge))

            rootView.edtxtEnteredTan.inputType = InputType.TYPE_CLASS_NUMBER
        }

        rootView.txtTanDescriptionToShowToUser.text = tanChallenge.messageToShowToUser

        rootView.btnCancel.setOnClickListener { enteringTanDone(null) }

        rootView.btnEnteringTanDone.setOnClickListener { enteringTanDone(rootView.edtxtEnteredTan.text.toString()) }
    }

    protected open fun setupSelectTanMediumView(rootView: View) {
        rootView.lytTanMedium.visibility = View.VISIBLE

        tanMediumAdapter.setItems(account.tanMedia.sortedByDescending { it.status == TanMediumStatus.Used })

        rootView.spnTanMedium.adapter = tanMediumAdapter
        rootView.spnTanMedium.onItemSelectedListener = ListItemSelectedListener(tanMediumAdapter) { selectedTanMedium ->
            if (selectedTanMedium.status != TanMediumStatus.Used) {
                presenter.changeTanMediumAsync(selectedTanMedium, account) { response ->
                    handleChangeTanMediumResponse(selectedTanMedium, response)
                }
            }
        }
    }

    private fun handleChangeTanMediumResponse(newUsedTanMedium: TanMedium, response: FinTsClientResponse) {
        activity?.let { activity ->
            activity.runOnUiThread {
                handleChangeTanMediumResponseOnUiThread(activity, newUsedTanMedium, response)
            }
        }
    }


    protected open fun handleChangeTanMediumResponseOnUiThread(context: Context, newUsedTanMedium: TanMedium, response: FinTsClientResponse) {
        if (response.isSuccessful) {
            dismiss()

            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_tan_medium_successfully_changed, newUsedTanMedium.displayName))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
        else {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_error_changing_tan_medium, newUsedTanMedium.displayName, presenter.getErrorToShowToUser(response)))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }


    protected open fun enteringTanDone(enteredTan: String?) {
        tanEnteredCallback(enteredTan)

        dismiss()
    }

}