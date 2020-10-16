package net.dankito.banking.ui.model.settings

import net.dankito.utils.multiplatform.UUID


open class AppSettings(
    open var automaticallyUpdateAccounts: Boolean = true,
    open var automaticallyUpdateAccountsAfterMinutes: Int = DefaultAutomaticallyUpdateAccountsAfterMinutes,
    open var lockAppAfterMinutes: Int? = null,
    open var flickerCodeSettings: TanMethodSettings? = null,
    open var qrCodeSettings: TanMethodSettings? = null,
    open var photoTanSettings: TanMethodSettings? = null
) {

    companion object {
        const val DefaultAutomaticallyUpdateAccountsAfterMinutes = 6 * 60 // 6 hours
    }


    internal constructor() : this(true, DefaultAutomaticallyUpdateAccountsAfterMinutes,  null, null, null) // for object deserializers


    open var technicalId: String = UUID.random()

}