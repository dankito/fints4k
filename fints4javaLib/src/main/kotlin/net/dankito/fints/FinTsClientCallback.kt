package net.dankito.fints

import net.dankito.fints.model.TanProcedure


interface FinTsClientCallback {

    fun askUserForTanProcedure(supportedTanProcedures: List<TanProcedure>): TanProcedure?

}