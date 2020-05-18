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
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.*


class ChipTanFlickerCodeView(private val flickerCode: FlickerCode): View() {

    companion object {
        private const val ChangeSizeStripeHeightStep = 7.0
        private const val ChangeSizeStripeWidthStep = 2.0
        private const val ChangeSizeSpaceBetweenStripesStep = 1.0

        const val MinFlickerCodeViewWidth = 124.0 + ChangeSizeStripeWidthStep + ChangeSizeSpaceBetweenStripesStep // below space between stripes aren't visible anymore
        const val MaxFlickerCodeViewWidth = 1000.0 // what is a senseful value?

        private const val ChangeFrequencyStep = 5

        private const val IconWidth = 26.0
        private const val IconHeight = 26.0
    }


    protected val flickerCodeLeftRightMargin = SimpleDoubleProperty(31.0)

    protected val stripeHeight = SimpleDoubleProperty(127.0)
    protected val stripeWidth = SimpleDoubleProperty(42.0)
    protected val spaceBetweenStripes = SimpleDoubleProperty(10.0)

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

    protected var currentFrequency = 20

    protected val animator = FlickerCodeAnimator()
    
    
    init {
        flickerCodeViewWidth.bind(stripeWidth.add(spaceBetweenStripes).multiply(4).add(stripeWidth).add(flickerCodeLeftRightMargin).add(flickerCodeLeftRightMargin))

        animator.setFrequency(currentFrequency)
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

                        stripeWidth.addListener { _, _, _ -> setLeftMarkerPosition(this) }
                    })

                    add(TanGeneratorMarkerView().apply {
                        setRightMarkerPosition(this)

                        stripeWidth.addListener { _, _, _ -> setRightMarkerPosition(this) }
                    })

                    vboxConstraints {
                        marginBottom = 6.0
                    }
                }

                hbox {
                    minHeight = 150.0

                    add(ChipTanFlickerCodeStripeView(stripe1, stripeWidth, stripeHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe2, stripeWidth, stripeHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe3, stripeWidth, stripeHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe4, stripeWidth, stripeHeight, spaceBetweenStripes))
                    add(ChipTanFlickerCodeStripeView(stripe5, stripeWidth, stripeHeight, SimpleDoubleProperty(0.0)))
                }

                vboxConstraints {
                    marginTopBottom(24.0)
                    marginLeftRight(flickerCodeLeftRightMargin.value)
                }
            }
        }

        animator.animateFlickerCode(flickerCode) { step ->
            runLater {
                paintFlickerCode(step)
            }
        }
    }


    private fun paintFlickerCode(step: Array<Bit>) {
        stripe1.set(step[0] == Bit.High)
        stripe2.set(step[1] == Bit.High)
        stripe3.set(step[2] == Bit.High)
        stripe4.set(step[3] == Bit.High)
        stripe5.set(step[4] == Bit.High)
    }

    private fun setLeftMarkerPosition(component: UIComponent) {
        component.root.anchorpaneConstraints {
            leftAnchor = (stripeWidth.value / 2)
        }
    }

    private fun setRightMarkerPosition(component: UIComponent) {
        component.root.anchorpaneConstraints {
            rightAnchor = (stripeWidth.value / 2)
        }
    }


    private fun increaseSize() {
        if (isMaxSizeReached.value == false) {
            stripeHeight.value = stripeHeight.value + ChangeSizeStripeHeightStep
            stripeWidth.value = stripeWidth.value + ChangeSizeStripeWidthStep
            spaceBetweenStripes.value = spaceBetweenStripes.value + ChangeSizeSpaceBetweenStripesStep
        }

        updateMinAndMaxSizeReached()
    }

    private fun decreaseSize() {
        if (isMinSizeReached.value == false) {
            stripeHeight.value = stripeHeight.value - ChangeSizeStripeHeightStep
            stripeWidth.value = stripeWidth.value - ChangeSizeStripeWidthStep
            spaceBetweenStripes.value = spaceBetweenStripes.value - ChangeSizeSpaceBetweenStripesStep
        }

        updateMinAndMaxSizeReached()
    }

    private fun updateMinAndMaxSizeReached() {
        val flickerCodeWidth = stripeWidth.value * 5 + spaceBetweenStripes.value * 4

        isMinSizeReached.value = flickerCodeWidth < MinFlickerCodeViewWidth
        isMaxSizeReached.value = flickerCodeWidth > MaxFlickerCodeViewWidth
    }

    private fun increaseFrequency() {
        if (isMaxFrequencyReached.value == false
                && (currentFrequency + ChangeFrequencyStep) <= FlickerCodeAnimator.MaxFrequency) {

            currentFrequency += ChangeFrequencyStep
            animator.setFrequency(currentFrequency)
        }

        updateMinAndMaxFrequencyReached()
    }

    private fun decreaseFrequency() {
        if (isMinFrequencyReached.value == false
                && (currentFrequency - ChangeFrequencyStep) >= FlickerCodeAnimator.MinFrequency) {

            currentFrequency -= ChangeFrequencyStep
            animator.setFrequency(currentFrequency)
        }

        updateMinAndMaxFrequencyReached()
    }

    private fun updateMinAndMaxFrequencyReached() {
        isMaxFrequencyReached.value = (currentFrequency + ChangeFrequencyStep) > FlickerCodeAnimator.MaxFrequency
        isMinFrequencyReached.value = (currentFrequency - ChangeFrequencyStep) < FlickerCodeAnimator.MinFrequency
    }

}