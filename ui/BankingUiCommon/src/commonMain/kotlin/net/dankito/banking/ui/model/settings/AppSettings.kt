package net.dankito.banking.ui.model.settings


open class AppSettings(
    var flickerCodeSettings: TanProcedureSettings? = null,
    var qrCodeSettings: TanProcedureSettings? = null,
    var photoTanSettings: TanProcedureSettings? = null
) {

    internal constructor() : this(null, null, null) // for object deserializers

}