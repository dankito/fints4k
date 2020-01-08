package net.dankito.banking.ui.javafx.dialogs.tan

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import net.dankito.banking.javafx.dialogs.tan.controls.ChipTanFlickerCodeView
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.FlickerCodeTanChallenge
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*


open class EnterTanDialog(
    protected val account: Account,
    protected val challenge: TanChallenge,
    protected val presenter: MainWindowPresenter,
    protected val tanEnteredCallback: (EnterTanResult) -> Unit
) : Window() {

    companion object {
        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    protected val enteredTan = SimpleStringProperty("")


    override val root = vbox {
        paddingAll = 4.0

        (challenge as? FlickerCodeTanChallenge)?.let { flickerCodeTanChallenge ->
            hbox {
                alignment = Pos.CENTER

                vboxConstraints {
                    marginLeftRight(30.0)
                    marginBottom = 12.0
                }

                add(ChipTanFlickerCodeView(flickerCodeTanChallenge.flickerCode))
            }
        }

        hbox {
            maxWidth = 400.0

            label(challenge.messageToShowToUser) {
                isWrapText = true
            }

            vboxConstraints {
                marginTopBottom(6.0)
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["enter.tan.dialog.enter.tan.label"])

            textfield(enteredTan) {
                prefHeight = 30.0
                prefWidth = 110.0

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


    private fun finishedEnteringTan() {
        if (enteredTan.value.isNullOrEmpty()) {
            cancelledEnteringTan()
        }
        else {
            tanEnteredCallback(EnterTanResult.userEnteredTan(enteredTan.value))

            close()
        }
    }

    private fun cancelledEnteringTan() {
        tanEnteredCallback(EnterTanResult.userDidNotEnterTan())

        close()
    }

}