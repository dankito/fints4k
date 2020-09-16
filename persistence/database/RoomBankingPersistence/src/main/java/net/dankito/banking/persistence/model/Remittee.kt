package net.dankito.banking.persistence.model

import androidx.room.ColumnInfo


data class Remittee(
    @ColumnInfo(name = "otherPartyName") val name: String,

    @ColumnInfo(name = "otherPartyBankCode") val bankCode: String?,

    @ColumnInfo(name = "otherPartyAccountId") val accountId: String?
)