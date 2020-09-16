package net.dankito.banking.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import net.dankito.banking.persistence.model.AccountTransaction
import net.dankito.banking.persistence.model.Remittee


@Dao
interface AccountTransactionDao : BaseDao<AccountTransaction> {

    @Query("SELECT * FROM AccountTransaction")
    fun getAll(): List<AccountTransaction>

    @Query("SELECT otherPartyName, otherPartyBankCode, otherPartyAccountId FROM AccountTransaction WHERE otherPartyName LIKE '%' || :query || '%'")
    fun findRemittees(query: String): List<Remittee>

}