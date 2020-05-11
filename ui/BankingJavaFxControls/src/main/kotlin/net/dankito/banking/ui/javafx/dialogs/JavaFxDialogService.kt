package net.dankito.banking.ui.javafx.dialogs

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.Window
import tornadofx.FX
import tornadofx.useMaxHeight
import tornadofx.useMaxWidth
import java.io.PrintWriter
import java.io.StringWriter


open class JavaFxDialogService  {

    open fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence? = null, owner: Stage? = FX.primaryStage) {
        Platform.runLater { showInfoMessageOnUiThread(infoMessage, alertTitle, owner) }
    }

    open fun showInfoMessageOnUiThread(infoMessage: CharSequence, alertTitle: CharSequence? = null, owner: Stage? = FX.primaryStage): ButtonType? {
        val dialog = createDialog(Alert.AlertType.INFORMATION, infoMessage, alertTitle, owner, ButtonType.OK)

        return showAndWaitForResult(dialog)
    }


    open fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence? = null, exception: Exception? = null, owner: Stage? = FX.primaryStage) {
        Platform.runLater { showErrorMessageOnUiThread(errorMessage, alertTitle, exception, owner) }
    }

    open fun showErrorMessageOnUiThread(errorMessage: CharSequence, alertTitle: CharSequence? = null, exception: Exception? = null, owner: Stage? = FX.primaryStage): ButtonType? {
        val dialog = createDialog(Alert.AlertType.ERROR, errorMessage, alertTitle, owner, ButtonType.OK)

        if (exception != null) {
            createExpandableException(dialog, exception)
        }

        return showAndWaitForResult(dialog)
    }

    protected open fun createExpandableException(dialog: Alert, exception: Exception) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val exceptionText = sw.toString()

        val label = Label("The exception stacktrace was:")

        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true

        textArea.useMaxWidth = true
        textArea.useMaxHeight = true
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expContent = GridPane()
        expContent.useMaxWidth = true
        expContent.add(label, 0, 0)
        expContent.add(textArea, 0, 1)

        // Set expandable Exception into the dialog pane.
        dialog.dialogPane.expandableContent = expContent
    }


    open fun showDialog(alertType: Alert.AlertType, message: CharSequence, alertTitle: CharSequence? = null, owner: Stage? = FX.primaryStage, vararg buttons: ButtonType): ButtonType? {
        val dialog = createDialog(alertType, message, alertTitle, owner, *buttons)

        return showAndWaitForResult(dialog)
    }

    protected open fun showAndWaitForResult(dialog: Alert): ButtonType? {
        val result = dialog.showAndWait()

        return result.map { it }.orElse(null)
    }

    open fun createDialog(alertType: Alert.AlertType, message: CharSequence, alertTitle: CharSequence?, owner: Stage?, vararg buttons: ButtonType): Alert {
        val alert = Alert(alertType)

        (alertTitle as? String)?.let { alert.title = it }

        owner?.let { alert.initOwner(it) }

        (message as? String)?.let { setAlertContent(alert, it) }
        alert.headerText = null

        alert.buttonTypes.setAll(*buttons)

        return alert
    }

    private fun setAlertContent(alert: Alert, content: String) {
        var maxWidth = Screen.getPrimary().visualBounds.width

        if(alert.owner != null) {
            getScreenWindowLeftUpperCornerIsIn(alert.owner)?.let { ownersScreen ->
                maxWidth = ownersScreen.visualBounds.width
            }
        }

        maxWidth *= 0.6 // set max width to 60 % of Screen width

        val contentLabel = Label(content)
        contentLabel.isWrapText = true
        contentLabel.prefHeight = Region.USE_COMPUTED_SIZE
        contentLabel.useMaxHeight = true
        contentLabel.maxWidth = maxWidth

        val contentPane = VBox(contentLabel)
        contentPane.prefHeight = Region.USE_COMPUTED_SIZE
        contentPane.useMaxHeight = true
        VBox.setVgrow(contentLabel, Priority.ALWAYS)

        alert.dialogPane.prefHeight = Region.USE_COMPUTED_SIZE
        alert.dialogPane.useMaxHeight = true
        alert.dialogPane.maxWidth = maxWidth
        alert.dialogPane.content = contentPane
    }

    protected open fun getScreenWindowLeftUpperCornerIsIn(window: Window): Screen? {
        val screens = Screen.getScreensForRectangle(window.x, window.y, 1.0, 1.0)
        if (screens.size > 0)
            return screens[0]

        return null
    }

}