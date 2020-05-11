package net.dankito.banking.javafx.dialogs.tan.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Node
import javafx.scene.paint.Color
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.View
import tornadofx.hboxConstraints
import tornadofx.label
import tornadofx.usePrefWidth


open class ChipTanFlickerCodeStripeView(protected val paintStripeWhite: SimpleBooleanProperty,
                                        protected val stripeWidth: SimpleDoubleProperty,
                                        protected val stripeHeight: SimpleDoubleProperty,
                                        protected val marginRight: SimpleDoubleProperty): View() {

    companion object {
        val Black = Color.BLACK
        val White = Color.WHITE
    }


    init {
        paintStripeWhite.addListener { _, _, newValue ->
            paintStripeWhiteChanged(newValue)
        }
    }


    override val root = label {
        prefHeightProperty().bind(stripeHeight)

        usePrefWidth = true
        prefWidthProperty().bind(stripeWidth)

        marginRight.addListener { _, _, newValue -> setMarginRight(this, newValue.toDouble()) }
        setMarginRight(this, marginRight.value)
    }

    private fun setMarginRight(node: Node, rightMargin: Double) {
        node.hboxConstraints {
            marginRight = rightMargin
        }
    }


    protected open fun paintStripeWhiteChanged(paintWhite: Boolean) {
        val newColor = if (paintWhite) White else Black

        root.setBackgroundToColor(newColor)
    }

}