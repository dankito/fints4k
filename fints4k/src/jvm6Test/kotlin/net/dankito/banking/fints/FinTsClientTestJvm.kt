package net.dankito.banking.fints

import net.dankito.banking.fints.util.*


class FinTsClientTestJvm : FinTsClientTestBase() {

    override fun createThreadPool(): IThreadPool {
        return JavaThreadPool()
    }

}