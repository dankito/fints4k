package net.dankito.banking.fints4java.android.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_enter_tan.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.adapter.TanMediumAdapter
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.TanMediumStatus
import net.dankito.fints.model.TanChallenge
import net.dankito.fints.model.TanProcedureType
import net.dankito.fints.tan.FlickercodeDecoder


open class EnterTanDialog : DialogFragment() {

    companion object {
        const val DialogTag = "EnterTanDialog"
    }


    protected lateinit var account: Account

    protected lateinit var tanChallenge: TanChallenge

    protected lateinit var tanEnteredCallback: (String?) -> Unit

    protected val tanMediumAdapter = TanMediumAdapter()


    open fun show(account: Account, tanChallenge: TanChallenge, activity: AppCompatActivity,
                  fullscreen: Boolean = false, tanEnteredCallback: (String?) -> Unit) {

        this.account = account
        this.tanChallenge = tanChallenge
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
                rootView.lytTanMedium.visibility = View.VISIBLE
                tanMediumAdapter.setItems(account.tanMedia.sortedByDescending { it.status == TanMediumStatus.Used })
                rootView.spnTanMedium.adapter = tanMediumAdapter
            }

            flickerCodeView.visibility = View.VISIBLE
            flickerCodeView.setCode(FlickercodeDecoder().decodeChallenge(tanChallenge.tanChallenge))

            rootView.edtxtEnteredTan.inputType = InputType.TYPE_CLASS_NUMBER
        }

        rootView.txtTanDescriptionToShowToUser.text = tanChallenge.messageToShowToUser

        rootView.btnCancel.setOnClickListener { enteringTanDone(null) }

        rootView.btnEnteringTanDone.setOnClickListener { enteringTanDone(rootView.edtxtEnteredTan.text.toString()) }
    }

    protected open fun enteringTanDone(enteredTan: String?) {
        tanEnteredCallback(enteredTan)

        dismiss()
    }

}