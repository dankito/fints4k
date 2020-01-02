package net.dankito.banking.fints4java.android.ui.dialogs

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import kotlinx.android.synthetic.main.dialog_enter_tan.view.*
import kotlinx.android.synthetic.main.view_tan_image.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.mapper.fints4javaModelMapper
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.adapter.TanMediumAdapter
import net.dankito.banking.fints4java.android.ui.adapter.TanProceduresAdapter
import net.dankito.banking.fints4java.android.ui.listener.ListItemSelectedListener
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.TanMedium
import net.dankito.banking.ui.model.TanMediumStatus
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.*
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.tan.TanImage


open class EnterTanDialog : DialogFragment() {

    companion object {
        val OpticalTanProcedures = listOf(TanProcedureType.ChipTanOptisch, TanProcedureType.ChipTanQrCode, TanProcedureType.PhotoTan)

        const val DialogTag = "EnterTanDialog"
    }


    protected lateinit var account: Account

    protected lateinit var tanChallenge: TanChallenge

    protected lateinit var presenter: MainWindowPresenter

    protected lateinit var tanEnteredCallback: (EnterTanResult) -> Unit

    protected val tanMediumAdapter = TanMediumAdapter()


    open fun show(account: Account, tanChallenge: TanChallenge, presenter: MainWindowPresenter, activity: AppCompatActivity,
                  fullscreen: Boolean = false, tanEnteredCallback: (EnterTanResult) -> Unit) {

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
        setupSelectTanProcedureView(rootView)

        setupTanView(rootView)

        rootView.txtTanDescriptionToShowToUser.text = tanChallenge.messageToShowToUser

        rootView.btnCancel.setOnClickListener { enteringTanDone(null) }

        rootView.btnEnteringTanDone.setOnClickListener { enteringTanDone(rootView.edtxtEnteredTan.text.toString()) }
    }

    protected open fun setupSelectTanProcedureView(rootView: View) {
        val adapter = TanProceduresAdapter()
        adapter.setItems(account.supportedTanProcedures)

        rootView.findViewById<Spinner>(R.id.spnTanProcedures)?.let { spinner ->
            spinner.adapter = adapter

            val selectedTanProcedure = account.selectedTanProcedure
                ?: account.supportedTanProcedures.firstOrNull()
            selectedTanProcedure?.let { spinner.setSelection(adapter.getItems().indexOf(selectedTanProcedure)) }

            spinner.onItemSelectedListener = ListItemSelectedListener(adapter) { newSelectedTanProcedure ->
                if (newSelectedTanProcedure != selectedTanProcedure) {
                    val mappedTanProcedure = fints4javaModelMapper().mapTanProcedureBack(newSelectedTanProcedure) // TODO: move to MainWindowPresenter
                    tanEnteredCallback(EnterTanResult.userAsksToChangeTanProcedure(mappedTanProcedure))
                    // TODO: find a way to update account.selectedTanProcedure afterwards

                    dismiss()
                }
            }
        }
    }

    protected open fun setupSelectTanMediumView(rootView: View) {
        rootView.lytTanMedium.visibility = View.VISIBLE

        tanMediumAdapter.setItems(account.tanMedia.sortedByDescending { it.status == TanMediumStatus.Used })

        rootView.spnTanMedium.adapter = tanMediumAdapter
        rootView.spnTanMedium.onItemSelectedListener = ListItemSelectedListener(tanMediumAdapter) { selectedTanMedium ->
            if (selectedTanMedium.status != TanMediumStatus.Used) {
                (selectedTanMedium.originalObject as? TanGeneratorTanMedium)?.let { tanGeneratorTanMedium ->
                    tanEnteredCallback(EnterTanResult.userAsksToChangeTanMedium(tanGeneratorTanMedium) { response ->
                        handleChangeTanMediumResponse(selectedTanMedium, response)
                    })
                    // TODO: find a way to update account.tanMedia afterwards
                }

                // TODO: what to do if newActiveTanMedium.originalObject is not of type TanGeneratorTanMedium?
            }
        }
    }

    protected open fun setupTanView(rootView: View) {
        if (OpticalTanProcedures.contains(tanChallenge.tanProcedure.type)) {
            if (account.tanMedia.isNotEmpty()) {
                setupSelectTanMediumView(rootView)
            }

            if (tanChallenge is FlickercodeTanChallenge) {
                val flickerCodeView = rootView.flickerCodeView
                flickerCodeView.visibility = View.VISIBLE
                flickerCodeView.setCode((tanChallenge as FlickercodeTanChallenge).flickercode)
            }
            else if (tanChallenge is ImageTanChallenge) {
                rootView.tanImageView.visibility = View.VISIBLE

                val decodedImage = (tanChallenge as ImageTanChallenge).image
                if (decodedImage.decodingSuccessful) {
                    val bitmap = BitmapFactory.decodeByteArray(decodedImage.imageBytes, 0, decodedImage.imageBytes.size)
                    rootView.imgTanImageView.setImageBitmap(bitmap)
                }
                else {
                    showDecodingTanImageFailedErrorDelayed(decodedImage) // this method gets called right on start up before dialog is shown -> Alert would get displayed before dialog and therefore covered by dialog if we don't delay displaying dialog
                }
            }

            rootView.edtxtEnteredTan.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }


    protected open fun showDecodingTanImageFailedErrorDelayed(decodedImage: TanImage) {
        val handler = Handler()

        handler.postDelayed({ showDecodingTanImageFailedError(decodedImage) }, 500)
    }

    protected open fun showDecodingTanImageFailedError(decodedImage: TanImage) {
        activity?.let { context ->
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_error_could_not_decode_tan_image, decodedImage.error?.localizedMessage))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }


    protected open fun handleChangeTanMediumResponse(newUsedTanMedium: TanMedium, response: FinTsClientResponse) {
        activity?.let { activity ->
            activity.runOnUiThread {
                handleChangeTanMediumResponseOnUiThread(activity, newUsedTanMedium, response)
            }
        }
    }

    protected open fun handleChangeTanMediumResponseOnUiThread(context: Context, newUsedTanMedium: TanMedium, response: FinTsClientResponse) {
        if (response.isSuccessful) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_tan_medium_successfully_changed, newUsedTanMedium.displayName))
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    dismiss()
                }
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
        val result = if (enteredTan != null) EnterTanResult.userEnteredTan(enteredTan) else EnterTanResult.userDidNotEnterTan()

        tanEnteredCallback(result)

        dismiss()
    }

}