package net.dankito.banking.ui.javafx.dialogs.tan.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.paint.Color
import net.dankito.banking.ui.model.tan.FlickerCode
import net.dankito.banking.ui.util.FlickerCodeAnimator
import net.dankito.banking.fints.tan.Bit
import net.dankito.banking.javafx.dialogs.tan.controls.ChipTanFlickerCodeStripeView
import net.dankito.banking.javafx.dialogs.tan.controls.TanGeneratorMarkerView
import net.dankito.banking.ui.model.settings.ITanView
import net.dankito.banking.ui.model.settings.TanProcedureSettings
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.*


open class ChipTanFlickerCodeView(
    protected val flickerCode: FlickerCode,
    tanProcedureSettings: TanProcedureSettings?
): View(), ITanView {

    companion object {
        const val ChangeSizeStripeHeightStep = 7.0
        const val ChangeSizeStripeWidthStep = 2.0
        const val ChangeSizeSpaceBetweenStripesStep = 1.0

        const val MinFlickerCodeViewWidth = 124.0 + ChangeSizeStripeWidthStep + ChangeSizeSpaceBetweenStripesStep // below space between stripes aren't visible anymore
        const val MaxFlickerCodeViewWidth = 1000.0 // what is a senseful value?

        const val ChangeFrequencyStep = 5
        const val DefaultFrequency = 30

        const val IconWidth = 26.0
        const val IconHeight = 26.0
    }


    protected val flickerCodeLeftRightMargin = SimpleDoubleProperty(31.0)

    protected val stripesHeight = SimpleDoubleProperty(tanProcedureSettings?.height?.toDouble() ?: 127.0)
    protected val stripesWidth = SimpleDoubleProperty(tanProcedureSettings?.width?.toDouble() ?: 42.0)
    protected val spaceBetweenStripes = SimpleDoubleProperty(tanProcedureSettings?.space?.toDouble() ?: 10.0)

    protected val flickerCodeViewWidth = SimpleDoubleProperty()

    protected val stripe1 = SimpleBooleanProperty()
    protected val stripe2 = SimpleBooleanProperty()
    protected val stripe3 = SimpleBooleanProperty()
    protected val stripe4 = SimpleBooleanProperty()
    protected val stripe5 = SimpleBooleanProperty()

    protected val isMinSizeReached = SimpleBooleanProperty(false)
    protected val isMaxSizeReached = SimpleBooleanProperty(false)

    protected val isMinFrequencyReached = SimpleBooleanProperty(false)
    protected val isMaxFrequencyReached = SimpleBooleanProperty(false)

    protected var currentFrequency = tanProcedureSettings?.frequency ?: DefaultFrequency

    protected val animator = FlickerCodeAnimator()


    override var didTanProcedureSettingsChange: Boolean = false
        protected set

    override var tanProcedureSettings: TanProcedureSettings? = tanProcedureSettings
        protected set
    
    
    init {
        flickerCodeViewWidth.bind(stripesWidth.add(spaceBetweenStripes).multiply(4).add(stripesWidth).add(flickerCodeLeftRightMargin).add(flickerCodeLeftRightMargin))

        setFrequency(currentFrequency)
    }


    override val root = vbox {
        alignment = Pos.CENTER
        usePrefHeight = true

        prefWidthProperty().bind(flickerCodeViewWidth)
        fixedWidth = flickerCodeViewWidth.value

        flickerCodeViewWidth.addListener { _, _, newValue ->
            fixedWidth = newValue.toDouble()
        }


        hbox {
            fixedHeight = IconHeight + 6.0

            alignment = Pos.CENTER

            add(TanImageSizeView(IconHeight, IconWidth, isMinSizeReached, isMaxSizeReached, { decreaseSize() }, { increaseSize() } ))

            label(messages["enter.tan.dialog.frequency.label"]) {
                hboxConstraints {
                    marginLeft = 12.0
                }
            }

            button("-") {
                prefHeight = IconHeight
                prefWidth = IconWidth

                disableWhen(isMinFrequencyReached)

                action { decreaseFrequency() }

                hboxConstraints {
                    marginLeft = 6.0
                    marginRight = 4.0
                }
            }

            button("+") {
                prefHeight = IconHeight
                prefWidth = IconWidth

                disableWhen(isMaxFrequencyReached)

                action { increaseFrequency() }
            }
        }

        vbox {
            setBackgroundToColor(Color.BLACK)

            vbox {
                anchorpane {

                    add(TanGeneratorMarkerView().apply {
                        setLeftMarkerPosition(this)

                        stripesWidth.addListener { _, _, _ -> setLeftMarkerPosition(this) }
                    })

                    add(TanGeneratorMarkerView().apply {
                        setRightMarkerPosition(this)

                        stripesWidth.addListener { _, _, _ -> setRightMarkerPosition(this) }
                    })

                    vboxConstraints {
                        marginBottom = 6.0
                    }
                }

                hbox {
                    minHeight = 150.0

                    add(ChipTanFlickerCodeStripeView(stripe1, stripesWidth, stripesHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe2, stripesWidth, stripesHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe3, stripesWidth, stripesHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe4, stripesWidth, stripesHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe5, stripesWidth, stripesHeight, SimpleDoubleProperty(0.0)))
                }

                vboxConstraints {
                    marginTopBottom(24.0)
                    marginLeftRight(flickerCodeLeftRightMargin.value)
                }
            }
        }


        updateMinAndMaxSizeReached()
        updateMinAndMaxFrequencyReached()

        animator.animateFlickerCode(flickerCode) { step ->
            runLater {
                paintFlickerCode(step)
            }
        }
    }


    protected open fun paintFlickerCode(step: Array<Bit>) {
        stripe1.set(step[0] == Bit.High)
        stripe2.set(step[1] == Bit.High)
        stripe3.set(step[2] == Bit.High)
        stripe4.set(step[3] == Bit.High)
        stripe5.set(step[4] == Bit.High)
    }

    protected open fun setLeftMarkerPosition(component: UIComponent) {
        component.root.anchorpaneConstraints {
            leftAnchor = (stripesWidth.value / 2)
        }
    }

    protected open fun setRightMarkerPosition(component: UIComponent) {
        component.root.anchorpaneConstraints {
            rightAnchor = (stripesWidth.value / 2)
        }
    }


    protected open fun increaseSize() {
        if (isMaxSizeReached.value == false) {
            setSize(stripesWidth.value + ChangeSizeStripeWidthStep, stripesHeight.value + ChangeSizeStripeHeightStep,
                spaceBetweenStripes.value + ChangeSizeSpaceBetweenStripesStep)

            tanProcedureSettingsChanged()
        }

        updateMinAndMaxSizeReached()
    }

    protected open fun decreaseSize() {
        if (isMinSizeReached.value == false) {
            setSize(stripesWidth.value - ChangeSizeStripeWidthStep, stripesHeight.value - ChangeSizeStripeHeightStep,
                spaceBetweenStripes.value - ChangeSizeSpaceBetweenStripesStep)

            tanProcedureSettingsChanged()
        }

        updateMinAndMaxSizeReached()
    }

    open fun setSize(width: Double, height: Double, spaceBetweenStripes: Double) {
        this.stripesWidth.value = width
        this.stripesHeight.value = height
        this.spaceBetweenStripes.value = spaceBetweenStripes

        tanProcedureSettingsChanged()

        updateMinAndMaxSizeReached()
    }

    protected open fun updateMinAndMaxSizeReached() {
        val flickerCodeWidth = stripesWidth.value * 5 + spaceBetweenStripes.value * 4

        isMinSizeReached.value = flickerCodeWidth < MinFlickerCodeViewWidth
        isMaxSizeReached.value = flickerCodeWidth > MaxFlickerCodeViewWidth
    }

    protected open fun increaseFrequency() {
        if (isMaxFrequencyReached.value == false
                && (currentFrequency + ChangeFrequencyStep) <= FlickerCodeAnimator.MaxFrequency) {

            setFrequency(currentFrequency + ChangeFrequencyStep)
        }

        updateMinAndMaxFrequencyReached()
    }

    protected open fun decreaseFrequency() {
        if (isMinFrequencyReached.value == false
                && (currentFrequency - ChangeFrequencyStep) >= FlickerCodeAnimator.MinFrequency) {

            setFrequency(currentFrequency - ChangeFrequencyStep)
        }

        updateMinAndMaxFrequencyReached()
    }

    protected open fun setFrequency(frequency: Int) {
        currentFrequency = frequency

        animator.setFrequency(currentFrequency)

        updateMinAndMaxFrequencyReached()

        tanProcedureSettingsChanged()
    }

    protected open fun updateMinAndMaxFrequencyReached() {
        isMaxFrequencyReached.value = (currentFrequency + ChangeFrequencyStep) > FlickerCodeAnimator.MaxFrequency
        isMinFrequencyReached.value = (currentFrequency - ChangeFrequencyStep) < FlickerCodeAnimator.MinFrequency
    }


    protected open fun tanProcedureSettingsChanged() {
        tanProcedureSettings = TanProcedureSettings(stripesWidth.value.toInt(), stripesHeight.value.toInt(),
            spaceBetweenStripes.value.toInt(), currentFrequency)

        didTanProcedureSettingsChange = true // we don't check if settings really changed, it's not that important
    }

}