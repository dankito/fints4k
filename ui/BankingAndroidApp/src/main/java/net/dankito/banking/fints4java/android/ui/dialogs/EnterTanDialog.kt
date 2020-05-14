package net.dankito.banking.fints4java.android.ui.dialogs

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_tan.view.*
import kotlinx.android.synthetic.main.view_tan_image.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.di.BankingComponent
import net.dankito.banking.fints4java.android.ui.adapter.TanMediumAdapter
import net.dankito.banking.fints4java.android.ui.adapter.TanProceduresAdapter
import net.dankito.banking.fints4java.android.ui.listener.ListItemSelectedListener
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.ui.presenter.BankingPresenter
import javax.inject.Inject


open class EnterTanDialog : DialogFragment() {

    companion object {
        val OpticalTanProcedures = listOf(TanProcedureType.ChipTanFlickercode, TanProcedureType.ChipTanQrCode,
            TanProcedureType.ChipTanPhotoTanMatrixCode, TanProcedureType.photoTan, TanProcedureType.QrCode)

        const val DialogTag = "EnterTanDialog"
    }


    protected lateinit var account: Account

    protected lateinit var tanChallenge: TanChallenge

    protected lateinit var tanEnteredCallback: (EnterTanResult) -> Unit

    protected val tanMediumAdapter = TanMediumAdapter()


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }


    open fun show(account: Account, tanChallenge: TanChallenge, activity: AppCompatActivity,
                  fullscreen: Boolean = false, tanEnteredCallback: (EnterTanResult) -> Unit) {

        this.account = account
        this.tanChallenge = tanChallenge
        this.tanEnteredCallback = tanEnteredCallback

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
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

        rootView.txtTanDescriptionToShowToUser.text = getString(R.string.dialog_enter_tan_hint_from_bank,  tanChallenge.messageToShowToUser)

        rootView.btnCancel.setOnClickListener { enteringTanDone(null) }

        rootView.btnEnteringTanDone.setOnClickListener { enteringTanDone(rootView.edtxtEnteredTan.text.toString()) }
    }

    protected open fun setupSelectTanProcedureView(rootView: View) {
        val adapter = TanProceduresAdapter()
        val tanProceduresWithoutUnsupported = account.supportedTanProcedures.filterNot { it.type == TanProcedureType.ChipTanUsb } // USB tan generators are not supported on Android
        adapter.setItems(tanProceduresWithoutUnsupported)

        rootView.findViewById<Spinner>(R.id.spnTanProcedures)?.let { spinner ->
            spinner.adapter = adapter

            val selectedTanProcedure = account.selectedTanProcedure
                ?: tanProceduresWithoutUnsupported.firstOrNull { it.type != TanProcedureType.ChipTanManuell && it.type != TanProcedureType.ChipTanUsb }
                ?: tanProceduresWithoutUnsupported.firstOrNull()
            selectedTanProcedure?.let { spinner.setSelection(adapter.getItems().indexOf(selectedTanProcedure)) }

            spinner.onItemSelectedListener = ListItemSelectedListener(adapter) { newSelectedTanProcedure ->
                if (newSelectedTanProcedure != selectedTanProcedure) {
                    tanEnteredCallback(EnterTanResult.userAsksToChangeTanProcedure(newSelectedTanProcedure))
                    // TODO: find a way to update account.selectedTanProcedure afterwards

                    dismiss()
                }
            }
        }
    }

    protected open fun setupSelectTanMediumView(rootView: View) {
        rootView.lytTanMedium.visibility = View.VISIBLE

        tanMediumAdapter.setItems(account.tanMediaSorted)

        rootView.spnTanMedium.adapter = tanMediumAdapter
        rootView.spnTanMedium.onItemSelectedListener = ListItemSelectedListener(tanMediumAdapter) { selectedTanMedium ->
            // TODO: implement logic to change a mobile phone as TAN medium
            if (selectedTanMedium.status != TanMediumStatus.Used) {
                (selectedTanMedium as? TanGeneratorTanMedium)?.let { tanGeneratorTanMedium ->
                    tanEnteredCallback(EnterTanResult.userAsksToChangeTanMedium(tanGeneratorTanMedium) { response ->
                        handleChangeTanMediumResponse(selectedTanMedium, response)
                    })
                    // TODO: find a way to update account.tanMedia afterwards
                }

                // TODO: ensure that only TanGeneratorTanMedium instances get added to spinner?
            }
        }
    }

    protected open fun setupTanView(rootView: View) {
        if (OpticalTanProcedures.contains(tanChallenge.tanProcedure.type)) {
            if (account.tanMedia.isNotEmpty()) {
                setupSelectTanMediumView(rootView)
            }

            if (tanChallenge is FlickerCodeTanChallenge) {
                val flickerCodeView = rootView.flickerCodeView
                flickerCodeView.visibility = View.VISIBLE

                val flickerCode = (tanChallenge as FlickerCodeTanChallenge).flickerCode
                if (flickerCode.decodingSuccessful) {
                    flickerCodeView.setCode(flickerCode)
                }
                else {
                    showDecodingTanChallengeFailedErrorDelayed(flickerCode.decodingError)
                }
            }
            else if (tanChallenge is ImageTanChallenge) {
                rootView.tanImageView.visibility = View.VISIBLE

                val decodedImage = (tanChallenge as ImageTanChallenge).image
                if (decodedImage.decodingSuccessful) {
                    val bitmap = BitmapFactory.decodeByteArray(decodedImage.imageBytes, 0, decodedImage.imageBytes.size)
                    rootView.imgTanImageView.setImageBitmap(bitmap)
                }
                else {
                    showDecodingTanChallengeFailedErrorDelayed(decodedImage.decodingError)
                }
            }

            rootView.edtxtEnteredTan.inputType = InputType.TYPE_CLASS_NUMBER

            rootView.edtxtEnteredTan.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    enteringTanDone(rootView.edtxtEnteredTan.text.toString())
                    return@setOnKeyListener true
                }
                false
            }
        }
    }


    /**
     * This method gets called right on start up before dialog is shown -> Alert would get displayed before dialog and
     * therefore covered by dialog -> delay displaying alert.
     */
    protected open fun showDecodingTanChallengeFailedErrorDelayed(error: Exception?) {
        val handler = Handler()

        handler.postDelayed({ showDecodingTanChallengeFailedError(error) }, 500)
    }

    protected open fun showDecodingTanChallengeFailedError(error: Exception?) {
        activity?.let { context ->
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_error_could_not_decode_tan_image, error?.localizedMessage))
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }


    protected open fun handleChangeTanMediumResponse(newUsedTanMedium: TanMedium, response: BankingClientResponse) {
        activity?.let { activity ->
            activity.runOnUiThread {
                handleChangeTanMediumResponseOnUiThread(activity, newUsedTanMedium, response)
            }
        }
    }

    protected open fun handleChangeTanMediumResponseOnUiThread(context: Context, newUsedTanMedium: TanMedium, response: BankingClientResponse) {
        if (response.isSuccessful) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_tan_medium_successfully_changed, newUsedTanMedium.displayName))
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    dismiss()
                }
                .show()
        }
        else if (response.userCancelledAction == false) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.dialog_enter_tan_error_changing_tan_medium, newUsedTanMedium.displayName, response.errorToShowToUser))
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