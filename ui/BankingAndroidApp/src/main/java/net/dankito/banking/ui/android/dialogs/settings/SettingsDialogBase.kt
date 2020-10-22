package net.dankito.banking.ui.android.dialogs.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.alerts.AskDismissChangesAlert
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.views.FormEditText
import net.dankito.banking.ui.presenter.BankingPresenter
import org.slf4j.LoggerFactory
import javax.inject.Inject


abstract class SettingsDialogBase : DialogFragment() {

    companion object {
        private val log = LoggerFactory.getLogger(SettingsDialogBase::class.java)
    }


    protected abstract val hasUnsavedChanges: Boolean

    protected abstract fun saveChanges()


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }



    fun show(activity: FragmentActivity, dialogTag: String, fullscreen: Boolean = true) {
        val style = if (fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, dialogTag)
    }


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                return@setOnKeyListener handleBackButtonPress()
            }

            false
        }
    }

    protected open fun handleBackButtonPress(): Boolean {
        askToDismissChanges()

        return hasUnsavedChanges
    }


    protected open fun setupToolbar(toolbar: Toolbar, dialogTitle: String, showSaveButton: Boolean = true) {
        toolbar.apply {
            title = dialogTitle

            inflateMenu(R.menu.menu_settings_dialog)
            menu.findItem(R.id.mnitmSaveChanges).isVisible = showSaveButton

            setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }

            setNavigationOnClickListener { askToDismissChanges() }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnitmSaveChanges -> saveChangesAndCloseDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }


    protected open fun didChange(editedValue: FormEditText, originalValue: String): Boolean {
        return editedValue.text != originalValue
    }


    protected open fun saveChangesAndCloseDialog(): Boolean {
        if (hasUnsavedChanges) {
            saveChanges()
        }

        closeDialog()

        return true
    }

    protected open fun askToDismissChanges() {
        if (hasUnsavedChanges) {
            AskDismissChangesAlert().show(this)
        }
        else {
            closeDialog()
        }
    }

    protected open fun closeDialog() {
        dismiss()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        log.info("Creating Fragment $this")
    }

    override fun onDismiss(dialog: DialogInterface) {
        log.info("Dismissing Fragment $this")

        super.onDismiss(dialog)
    }

    override fun onPause() {
        log.info("Pausing Fragment $this")

        super.onPause()
    }

    override fun onStop() {
        log.info("Stopping Fragment $this")

        super.onStop()
    }

    override fun onDestroy() {
        log.info("Destroying Fragment $this")

        super.onDestroy()
    }

}