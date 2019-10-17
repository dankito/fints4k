package net.dankito.fints

import net.dankito.fints.model.TanChallenge
import net.dankito.fints.model.TanProcedure


interface FinTsClientCallback {

    fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure?

    fun enterTan(tanChallenge: TanChallenge): String?

}