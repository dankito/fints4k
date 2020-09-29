package net.dankito.banking.ui.model.settings

import net.dankito.utils.multiplatform.UUID


open class AppSettings(
    open var flickerCodeSettings: TanMethodSettings? = null,
    open var qrCodeSettings: TanMethodSettings? = null,
    open var photoTanSettings: TanMethodSettings? = null
) {

    internal constructor() : this(null, null, null) // for object deserializers


    open var technicalId: String = UUID.random()

}