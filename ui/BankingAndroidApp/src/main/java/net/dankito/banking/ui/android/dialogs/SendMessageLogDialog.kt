package net.dankito.banking.ui.android.dialogs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_send_message_log.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.presenter.BankingPresenter
import javax.inject.Inject


open class SendMessageLogDialog : DialogFragment() {

    companion object {
        const val DialogTag = "SendMessageLogDialog"
    }


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }


    fun show(activity: AppCompatActivity, fullscreen: Boolean = false) {
        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_send_message_log, container, false)

        rootView?.let {
            setupUI(rootView)
        }

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        val messageLog = presenter.getMessageLogForAccounts(presenter.accounts).joinToString("\r\n\r\n")

        if (messageLog.isBlank()) {
            rootView.txtvwInfoNoMessageLogEntriesYet.visibility = View.VISIBLE
            rootView.lytMessageLog.visibility = View.GONE
        }
        else {
            rootView.edtxtMessageLog.setText(context?.getString(R.string.dialog_send_message_courteously_add_error_description, messageLog))
        }

        rootView.btnSendMessageLog.setOnClickListener { sendMessageLog(rootView.edtxtMessageLog.text.toString()) }

        rootView.btnCancel.setOnClickListener { dismiss() }
    }

    protected open fun sendMessageLog(messageLog: String) {
        val sendMailActivity = Intent(Intent.ACTION_SEND)
        sendMailActivity.type = "message/rfc822"
        sendMailActivity.putExtra(Intent.EXTRA_EMAIL, arrayOf("panta.rhei@dankito.net"))
        sendMailActivity.putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.dialog_send_message_log_mail_subject))
        sendMailActivity.putExtra(Intent.EXTRA_TEXT, messageLog)

        try {
            startActivity(Intent.createChooser(sendMailActivity, context?.getString(R.string.dialog_send_message_log_action_send_chooser_title)))

            Toast.makeText(context, context?.getString(R.string.dialog_send_message_log_thanks_for_helping_making_app_better), Toast.LENGTH_LONG).show()

            dismiss()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context?.getString(R.string.dialog_send_message_log_error_no_app_to_send_message_found), Toast.LENGTH_LONG).show()
        }
    }

}