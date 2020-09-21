package net.dankito.banking.ui.model.settings


open class AppSettings(
    var flickerCodeSettings: TanMethodSettings? = null,
    var qrCodeSettings: TanMethodSettings? = null,
    var photoTanSettings: TanMethodSettings? = null
) {

    internal constructor() : this(null, null, null) // for object deserializers

}