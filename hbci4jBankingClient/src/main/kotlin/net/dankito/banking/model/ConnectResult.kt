package net.dankito.banking.model

import org.kapott.hbci.manager.HBCIHandler
import org.kapott.hbci.passport.HBCIPassport


open class ConnectResult(
    val successful: Boolean,
    val error: Exception? = null,
    val handle: HBCIHandler? = null,
    val passport: HBCIPassport? = null
)