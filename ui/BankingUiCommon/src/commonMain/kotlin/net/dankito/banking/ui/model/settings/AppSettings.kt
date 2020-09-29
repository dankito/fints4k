package net.dankito.banking.ui.model.settings

import net.dankito.utils.multiplatform.UUID


open class AppSettings(
    open var updateAccountsAutomatically: Boolean = true,
    open var refreshAccountsAfterMinutes: Int = DefaultRefreshAccountsAfterMinutes,
    open var flickerCodeSettings: TanMethodSettings? = null,
    open var qrCodeSettings: TanMethodSettings? = null,
    open var photoTanSettings: TanMethodSettings? = null
) {

    companion object {
        const val DefaultRefreshAccountsAfterMinutes = 8 * 60 // 8 hours
    }


    internal constructor() : this(true, DefaultRefreshAccountsAfterMinutes, null, null) // for object deserializers


    open var technicalId: String = UUID.random()

}