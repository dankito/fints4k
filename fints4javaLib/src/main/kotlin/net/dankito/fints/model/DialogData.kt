package net.dankito.fints.model


open class DialogData(
    var dialogId: String = "0",
    var messageNumber: Int = 1
) {

    companion object {
        val DialogInitDialogData = DialogData("0", 1)
    }

}