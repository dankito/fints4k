package net.dankito.banking.ui.android.dialogs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_enter_tan.*
import kotlinx.android.synthetic.main.dialog_enter_tan.view.*
import kotlinx.android.synthetic.main.view_collapsible_text.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.TanMediumAdapter
import net.dankito.banking.ui.android.adapter.TanMethodsAdapter
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.listener.ListItemSelectedListener
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.getSpannedFromHtml
import javax.inject.Inject


open class EnterTanDialog : DialogFragment() {

    companion object {
        const val DialogTag = "EnterTanDialog"
    }


    protected lateinit var bank: TypedBankData

    protected lateinit var tanChallenge: TanChallenge

    protected lateinit var tanEnteredCallback: (EnterTanResult) -> Unit

    protected val tanMediumAdapter = TanMediumAdapter()


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }


    open fun show(bank: TypedBankData, tanChallenge: TanChallenge, activity: AppCompatActivity,
                  fullscreen: Boolean = false, tanEnteredCallback: (EnterTanResult) -> Unit) {

        this.bank = bank
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
        setupSelectTanMethodView(rootView)

        setupTanView(rootView)

        setupEnteringTan(rootView)

        rootView.txtvwCollapsibleText.text = tanChallenge.messageToShowToUser.getSpannedFromHtml()

        rootView.btnCancel.setOnClickListener { enteringTanDone(null) }

        rootView.btnEnteringTanDone.setOnClickListener { enteringTanDone(rootView.edtxtEnteredTan.text.toString()) }
    }

    protected open fun setupSelectTanMethodView(rootView: View) {
        val adapter = TanMethodsAdapter()
        val tanMethodsWithoutUnsupported = bank.supportedTanMethods.filterNot { it.type == TanMethodType.ChipTanUsb } // USB tan generators are not supported on Android
        adapter.setItems(tanMethodsWithoutUnsupported)

        rootView.findViewById<Spinner>(R.id.spnTanMethods)?.let { spinner ->
            spinner.adapter = adapter

            val selectedTanMethod = bank.selectedTanMethod
                ?: tanMethodsWithoutUnsupported.firstOrNull { it.type != TanMethodType.ChipTanManuell && it.type != TanMethodType.ChipTanUsb }
                ?: tanMethodsWithoutUnsupported.firstOrNull()
            selectedTanMethod?.let { spinner.setSelection(adapter.getItems().indexOf(selectedTanMethod)) }

            spinner.onItemSelectedListener = ListItemSelectedListener(adapter) { newSelectedTanMethod ->
                if (newSelectedTanMethod != selectedTanMethod) {
                    tanEnteredCallback(EnterTanResult.userAsksToChangeTanMethod(newSelectedTanMethod))
                    // TODO: find a way to update account.selectedTanMethod afterwards

                    dismiss()
                }
            }
        }
    }

    protected open fun setupSelectTanMediumView(rootView: View) {
        val tanMediaForTanMethod = presenter.getTanMediaForTanMethod(bank, tanChallenge.tanMethod)

        if (tanMediaForTanMethod.size > 1) {
            rootView.lytTanMedium.visibility = View.VISIBLE

            tanMediumAdapter.setItems(bank.tanMediaSorted)

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
    }

    protected open fun setupTanView(rootView: View) {
        if (presenter.isOpticalTanMethod(tanChallenge.tanMethod)) {
            setupSelectTanMediumView(rootView)

            if (tanChallenge is FlickerCodeTanChallenge) {
                setupFlickerCodeTanView(rootView)
            }
            else if (tanChallenge is ImageTanChallenge) {
                setupImageTanView(rootView)
            }
        }
    }

    protected open fun setupEnteringTan(rootView: View) {
        if (tanChallenge.tanMethod.isNumericTan) {
            rootView.edtxtEnteredTan.inputType = InputType.TYPE_CLASS_NUMBER
        }

        tanChallenge.tanMethod.maxTanInputLength?.let { maxInputLength ->
            rootView.edtxtEnteredTan.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxInputLength))
        }

        rootView.edtxtEnteredTan.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                enteringTanDone(rootView.edtxtEnteredTan.text.toString())
                return@setOnKeyListener true
            }
            false
        }
    }

    protected open fun setupFlickerCodeTanView(rootView: View) {
        val flickerCodeView = rootView.flickerCodeView
        flickerCodeView.visibility = View.VISIBLE

        val flickerCode = (tanChallenge as FlickerCodeTanChallenge).flickerCode
        if (flickerCode.decodingSuccessful) {
            flickerCodeView.setCode(flickerCode, presenter.appSettings.flickerCodeSettings)
        }
        else {
            showDecodingTanChallengeFailedErrorDelayed(flickerCode.decodingError)
        }
    }

    protected open fun setupImageTanView(rootView: View) {
        rootView.tanImageView.visibility = View.VISIBLE

        val decodedImage = (tanChallenge as ImageTanChallenge).image
        if (decodedImage.decodingSuccessful) {
            rootView.tanImageView.setImage(tanChallenge as ImageTanChallenge, if (isQrTan(tanChallenge)) presenter.appSettings.qrCodeSettings else presenter.appSettings.photoTanSettings)
        }
        else {
            showDecodingTanChallengeFailedErrorDelayed(decodedImage.decodingError)
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
        if (response.successful) {
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

        checkIfAppSettingsChanged()

        dismiss()
    }


    protected open fun checkIfAppSettingsChanged() {
        if (flickerCodeView.didTanMethodSettingsChange) {
            presenter.updateTanMethodSettings(tanChallenge.tanMethod, flickerCodeView.tanMethodSettings)
        }

        if (tanImageView.didTanMethodSettingsChange) {
            presenter.updateTanMethodSettings(tanChallenge.tanMethod, tanImageView.tanMethodSettings)
        }
    }

    protected open fun isQrTan(tanChallenge: TanChallenge): Boolean {
        return presenter.isQrTanMethod(tanChallenge.tanMethod)
    }

}