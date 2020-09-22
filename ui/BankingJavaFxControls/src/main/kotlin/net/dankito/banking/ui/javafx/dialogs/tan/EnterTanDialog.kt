package net.dankito.banking.ui.javafx.dialogs.tan

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Region
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.banking.ui.javafx.dialogs.tan.controls.ChipTanFlickerCodeView
import net.dankito.banking.ui.javafx.dialogs.JavaFxDialogService
import net.dankito.banking.ui.javafx.dialogs.tan.controls.TanImageView
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.extensions.htmlToPlainText
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*


open class EnterTanDialog(
    protected val bank: TypedBankData,
    protected val challenge: TanChallenge,
    protected val presenter: BankingPresenter,
    protected val tanEnteredCallback: (EnterTanResult) -> Unit
) : Window() {

    companion object {
        val QrCodeTanMethods = listOf(TanMethodType.ChipTanQrCode, TanMethodType.QrCode)

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    protected val dialogService = JavaFxDialogService()

    protected var flickerCodeView: ChipTanFlickerCodeView? = null

    protected var tanImageView: TanImageView? = null


    protected val tanMethodsWithoutUnsupported = bank.supportedTanMethods.filterNot { it.type == TanMethodType.ChipTanUsb } // USB tan generators are not supported

    protected val selectedTanMethod = SimpleObjectProperty<TanMethod>(bank.selectedTanMethod ?: tanMethodsWithoutUnsupported.firstOrNull { it.displayName.contains("manuell", true) == false } ?: tanMethodsWithoutUnsupported.firstOrNull())

    protected val selectedTanMedium = SimpleObjectProperty<TanMedium>(bank.tanMediaSorted.firstOrNull())

    protected val enteredTan = SimpleStringProperty("")


    init {
        selectedTanMethod.addListener { _, _, newValue ->
            tanEnteredCallback(EnterTanResult.userAsksToChangeTanMethod(newValue))

            close()
        }

        selectedTanMedium.addListener { _, _, newValue ->
            if (newValue.status != TanMediumStatus.Used) {
                tanEnteredCallback(EnterTanResult.userAsksToChangeTanMedium(newValue) { response ->
                    runLater { handleChangeTanMediumResponseOnUiThread(newValue, response) }
                })
            }
        }
    }


    override val root = vbox {
        paddingAll = 4.0

        form {
            fieldset {
                field(messages["enter.tan.dialog.select.tan.method"]) {
                    label.apply {
                        font = Font.font(font.family, FontWeight.BLACK, font.size)
                    }

                    combobox(selectedTanMethod, tanMethodsWithoutUnsupported) {
                        cellFormat {
                            text = it.displayName
                        }
                    }
                }

                if (bank.tanMediaSorted.isNotEmpty()) {
                    field(messages["enter.tan.dialog.select.tan.medium"]) {
                        label.apply {
                            font = Font.font(font.family, FontWeight.BLACK, font.size)
                        }

                        combobox(selectedTanMedium, bank.tanMediaSorted) {
                            cellFormat {
                                text = it.displayName
                            }
                        }
                    }
                }
            }
        }

        (challenge as? FlickerCodeTanChallenge)?.let { flickerCodeTanChallenge ->
            val flickerCode = flickerCodeTanChallenge.flickerCode
            if (flickerCode.decodingSuccessful) {
                hbox {
                    alignment = Pos.CENTER

                    vboxConstraints {
                        marginLeftRight(30.0)
                        marginBottom = 12.0
                    }

                    add(ChipTanFlickerCodeView(flickerCode, presenter.appSettings.flickerCodeSettings).apply {
                        flickerCodeView = this
                    })
                }
            }
            else {
                showDecodingTanChallengeFailedErrorOnViewInitialization(flickerCode.decodingError)
            }
        }

        (challenge as? ImageTanChallenge)?.let { imageTanChallenge ->
            val decodedImage = imageTanChallenge.image
            if (decodedImage.decodingSuccessful) {
                add(TanImageView(decodedImage, if (isQrTan(challenge)) presenter.appSettings.qrCodeSettings else presenter.appSettings.photoTanSettings).apply {
                    tanImageView = this

                    vboxConstraints {
                        marginLeftRight(30.0)
                        marginBottom = 12.0
                    }
                })
            }
            else {
                showDecodingTanChallengeFailedErrorOnViewInitialization(decodedImage.decodingError)
            }
        }

        label(messages["enter.tan.dialog.hint.from.bank"]) {
            font = Font.font(font.family, FontWeight.BLACK, font.size)

            vboxConstraints {
                marginTopBottom(6.0)
            }
        }

//        // TODO: also display rich text like <b>, not only new line
        label(challenge.messageToShowToUser.htmlToPlainText()) {
            useMaxWidth = true
            minWidth = Region.USE_PREF_SIZE

            isWrapText = true

            vboxConstraints {
                marginBottom = 18.0
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["enter.tan.dialog.enter.tan.label"]) {
                font = Font.font(font.family, FontWeight.BLACK, font.size)
            }

            textfield(enteredTan) {
                prefHeight = 30.0
                prefWidth = 150.0

                runLater {
                    requestFocus()
                }

                hboxConstraints {
                    marginLeft = 6.0
                }
            }
        }

        hbox {
            alignment = Pos.CENTER_RIGHT

            button(messages["cancel"]) {
                prefHeight = ButtonHeight
                prefWidth = ButtonWidth

                isCancelButton = true

                action { cancelledEnteringTan() }

                hboxConstraints {
                    margin = Insets(6.0, 0.0, 4.0, 0.0)
                }
            }

            button(messages["ok"]) {
                prefHeight = ButtonHeight
                prefWidth = ButtonWidth

                isDefaultButton = true

                action { finishedEnteringTan() }

                hboxConstraints {
                    margin = Insets(6.0, 4.0, 4.0, 12.0)
                }
            }

            vboxConstraints {
                marginTop = 4.0
            }
        }
    }


    protected open fun showDecodingTanChallengeFailedErrorOnViewInitialization(error: Exception?) {
        runLater {
            showDecodingTanChallengeFailedError(error)
        }
    }

    protected open fun showDecodingTanChallengeFailedError(error: Exception?) {
        dialogService.showErrorMessage(String.format(messages["enter.tan.dialog.error.could.not.decode.tan.image"], error?.localizedMessage),
            null, error, currentStage)
    }

    protected open fun handleChangeTanMediumResponseOnUiThread(newUsedTanMedium: TanMedium, response: BankingClientResponse) {
        if (response.successful) {
            dialogService.showInfoMessageOnUiThread(String.format(messages["enter.tan.dialog.tan.medium.successfully.changed"],
                newUsedTanMedium.displayName), null, currentStage)

            close()
        }
        else if (response.userCancelledAction == false) {
            dialogService.showErrorMessageOnUiThread(String.format(messages["enter.tan.dialog.tan.error.changing.tan.medium"],
                newUsedTanMedium.displayName, response.errorToShowToUser), null, null, currentStage)
        }
    }


    protected open fun finishedEnteringTan() {
        if (enteredTan.value.isNullOrEmpty()) {
            cancelledEnteringTan()
        }
        else {
            tanEnteredCallback(EnterTanResult.userEnteredTan(enteredTan.value))

            checkIfAppSettingsChanged()

            close()
        }
    }

    protected open fun cancelledEnteringTan() {
        tanEnteredCallback(EnterTanResult.userDidNotEnterTan())

        close()
    }


    protected open fun checkIfAppSettingsChanged() {
        if (flickerCodeView?.didTanMethodSettingsChange == true) {
            presenter.appSettings.flickerCodeSettings = flickerCodeView?.tanMethodSettings

            presenter.appSettingsChanged()
        }

        if (tanImageView?.didTanMethodSettingsChange == true) {
            if (isQrTan(challenge)) {
                presenter.appSettings.qrCodeSettings = tanImageView?.tanMethodSettings
            }
            else {
                presenter.appSettings.photoTanSettings = tanImageView?.tanMethodSettings
            }

            presenter.appSettingsChanged()
        }
    }

    protected open fun isQrTan(tanChallenge: TanChallenge): Boolean {
        return QrCodeTanMethods.contains(tanChallenge.tanMethod.type)
    }

}