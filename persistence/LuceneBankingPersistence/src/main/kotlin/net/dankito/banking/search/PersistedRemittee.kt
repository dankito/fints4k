package net.dankito.banking.search

import net.dankito.utils.lucene.mapper.IdentifiableByString


class PersistedRemittee(
    val otherPartyName: String?,
    val otherPartyBankCode: String?,
    val otherPartyAccountId: String?
) : IdentifiableByString {

    override var id: String = ""


    internal constructor() : this("", "", "")

}