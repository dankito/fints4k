package net.dankito.banking.ui.javafx.dialogs.tan.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import tornadofx.*


open class TanImageSizeView(iconHeight: Double, iconWidth: Double,
                            isMinSizeReached: SimpleBooleanProperty, isMaxSizeReached: SimpleBooleanProperty,
                            protected val decreaseSizeAction: (() -> Unit), protected val increaseSizeAction: (() -> Unit)) : View() {


    override val root = hbox {
        prefHeight = iconHeight + 6.0

        alignment = Pos.CENTER

        label(messages["enter.tan.dialog.size.label"])

        button("-") {
            prefHeight = iconHeight
            prefWidth = iconWidth

            disableWhen(isMinSizeReached)

            action { decreaseSizeAction() }

            hboxConstraints {
                marginLeft = 6.0
                marginRight = 4.0
            }
        }

        button("+") {
            prefHeight = iconHeight
            prefWidth = iconWidth

            disableWhen(isMaxSizeReached)

            action { increaseSizeAction() }
        }
    }

}