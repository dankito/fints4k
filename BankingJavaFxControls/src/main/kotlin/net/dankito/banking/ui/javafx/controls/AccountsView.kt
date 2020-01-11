package net.dankito.banking.ui.javafx.controls

import javafx.geometry.Pos
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import tornadofx.*


open class AccountsView(protected val presenter: MainWindowPresenter) : View() {


    override val root = vbox {
        borderpane {
            fixedHeight = 36.0

            left = label(messages["accounts"]) {
                borderpaneConstraints {
                    alignment = Pos.CENTER_LEFT
                    marginLeft = 4.0
                }
            }

            right = addButton(fontSize = 14.0) {
                fixedHeight = 32.0
                fixedWidth = 32.0

                action { showAddAccountDialog() }

                borderpaneConstraints {
                    alignment = Pos.CENTER_RIGHT
                    marginTopBottom(2.0)
                }
            }
        }

    }

    private fun showAddAccountDialog() {
        presenter.showAddAccountDialog()
    }

}