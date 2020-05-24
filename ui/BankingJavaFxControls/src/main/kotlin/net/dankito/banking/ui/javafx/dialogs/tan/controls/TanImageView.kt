package net.dankito.banking.ui.javafx.dialogs.tan.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import net.dankito.banking.ui.model.settings.ITanView
import net.dankito.banking.ui.model.settings.TanProcedureSettings
import net.dankito.banking.ui.model.tan.TanImage
import net.dankito.utils.javafx.ui.extensions.updateWindowSize
import tornadofx.*
import java.io.ByteArrayInputStream


open class TanImageView(
    protected val tanImage: TanImage,
    tanProcedureSettings: TanProcedureSettings?
) : View(), ITanView {

    companion object {
        private const val ChangeSizeStepSize = 10.0

        const val MinHeight = 100.0
        const val MaxHeight = 500.0

        private const val IconHeight = 26.0
        private const val IconWidth = 26.0
    }


    protected val isMinSizeReached = SimpleBooleanProperty(false)
    protected val isMaxSizeReached = SimpleBooleanProperty(false)

    protected var tanImageView: ImageView by singleAssign()


    override var didTanProcedureSettingsChange: Boolean = false
        protected set

    override var tanProcedureSettings: TanProcedureSettings? = tanProcedureSettings
        protected set


    override val root = vbox {
        add(TanImageSizeView(IconHeight, IconWidth, isMinSizeReached, isMaxSizeReached, { decreaseSize() }, { increaseSize() } ))

        tanImageView = imageview(Image(ByteArrayInputStream(tanImage.imageBytes))) {
            fitHeight = 250.0

            alignment = Pos.CENTER

            isPreserveRatio = true

            vboxConstraints {
                marginTop = 8.0
                marginLeftRight(30.0)
                marginBottom = 4.0
            }
        }

        tanProcedureSettings?.let {
            runLater {
                setWidthAndHeight(it.width.toDouble())
            }
        }
    }


    open fun increaseSize() {
        changeSizeBy(ChangeSizeStepSize)
    }

    open fun decreaseSize() {
        changeSizeBy(ChangeSizeStepSize * -1)
    }

    protected open fun changeSizeBy(changeSizeBy: Double) {
        val newWidthAndHeight = tanImageView.fitHeight + changeSizeBy

        setWidthAndHeight(newWidthAndHeight)
    }

    protected open fun setWidthAndHeight(newWidthAndHeight: Double) {
        if (newWidthAndHeight in MinHeight..MaxHeight) {
            tanImageView.fitHeight = newWidthAndHeight

            updateWindowSize()

            tanProcedureSettingsChanged(newWidthAndHeight.toInt())
        }

        isMinSizeReached.value = tanImageView.fitHeight <= MinHeight
        isMaxSizeReached.value = tanImageView.fitHeight >= MaxHeight
    }

    protected open fun tanProcedureSettingsChanged(newWidthAndHeight: Int) {
        tanProcedureSettings = TanProcedureSettings(newWidthAndHeight, newWidthAndHeight)

        didTanProcedureSettingsChange = true // we don't check if settings really changed, it's not that important
    }

}