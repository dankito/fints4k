package net.dankito.banking.ui.javafx.dialogs.addaccount

import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import net.dankito.banking.bankfinder.BankInfo
import tornadofx.*


open class BankInfoListCellFragment :  ListCellFragment<BankInfo>() {

    companion object {
        const val ItemHeight = 40.0

        val Fints30SupportedIcon = Image("icons/bank_supports_fints_3_0.png")

        val Fints30NotSupportedIcon = Image("icons/bank_does_not_support_fints_3_0.png")
    }


    open val bank = BankInfoViewModel().bindTo(this)


    override val root = hbox {
        prefHeight = ItemHeight

        paddingTop = 4.0
        paddingBottom = 4.0

        imageview {
            alignment = Pos.CENTER
            fitWidth = ItemHeight
            isPreserveRatio = true

            setFints3SupportedOrNotIcon(this)

            bank.supportsFinTs3_0.addListener { _, _, _ -> setFints3SupportedOrNotIcon(this) }
        }

        vbox {
            hboxConstraints {
                hGrow = Priority.ALWAYS

                marginLeft = 4.0
            }

            label(bank.bankName) {
                useMaxWidth = true

                font = Font.font(14.0)
            }

            hbox {
                alignment = Pos.CENTER_LEFT

                label(bank.bankCode)

                label(bank.bankAddress) {
                    useMaxWidth = true

                    hboxConstraints {
                        marginLeft = 12.0
                    }
                }

                vboxConstraints {
                    marginTop = 6.0
                }
            }
        }
    }

    protected open fun setFints3SupportedOrNotIcon(imageView: ImageView) {
        if (bank.supportsFinTs3_0.value) {
            imageView.image = Fints30SupportedIcon
        }
        else {
            imageView.image = Fints30NotSupportedIcon
        }
    }

}