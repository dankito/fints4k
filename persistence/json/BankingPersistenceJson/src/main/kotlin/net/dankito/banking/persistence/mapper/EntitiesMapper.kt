package net.dankito.banking.persistence.mapper

import net.dankito.banking.persistence.model.AccountTransactionEntity
import net.dankito.banking.persistence.model.BankAccountEntity
import net.dankito.banking.persistence.model.CustomerEntity
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.Customer


open class EntitiesMapper {

    open fun mapCustomers(customers: List<Customer>): List<CustomerEntity> {
        return customers.map { mapCustomer(it) }
    }

    open fun mapCustomer(customer: Customer): CustomerEntity {
        val mappedCustomer = CustomerEntity(
            customer.bankCode, customer.customerId, customer.password, customer.finTsServerAddress,
            customer.bankName, customer.bic, customer.customerName, customer.userId, customer.iconUrl,
            listOf(), customer.supportedTanProcedures, customer.selectedTanProcedure, customer.tanMedia
        )

        mappedCustomer.id = customer.technicalId
        mappedCustomer.userSetDisplayName = customer.userSetDisplayName

        mappedCustomer.accounts = mapBankAccounts(customer.accounts, mappedCustomer)

        return mappedCustomer
    }


    open fun mapCustomerEntities(customers: List<CustomerEntity>): List<Customer> {
        return customers.map { mapCustomer(it) }
    }

    open fun mapCustomer(customer: CustomerEntity): Customer {
        val mappedCustomer = Customer(
            customer.bankCode, customer.customerId, customer.password, customer.finTsServerAddress,
            customer.bankName, customer.bic, customer.customerName, customer.userId, customer.iconUrl
        )

        mappedCustomer.technicalId = customer.id
        mappedCustomer.userSetDisplayName = customer.userSetDisplayName

        mappedCustomer.accounts = mapBankAccounts(customer.accounts, mappedCustomer)


        mappedCustomer.supportedTanProcedures = customer.supportedTanProcedures
        mappedCustomer.selectedTanProcedure = customer.selectedTanProcedure
        mappedCustomer.tanMedia = customer.tanMedia

        return mappedCustomer
    }


    open fun mapBankAccounts(transactions: List<BankAccount>, customer: CustomerEntity): List<BankAccountEntity> {
        return transactions.map { mapBankAccount(it, customer) }
    }

    open fun mapBankAccount(account: BankAccount, customer: CustomerEntity): BankAccountEntity {
        val mappedAccount = BankAccountEntity(
            customer, account.identifier, account.accountHolderName, account.iban, account.subAccountNumber,
            account.customerId, account.balance, account.currency, account.type, account.productName,
            account.accountLimit, account.lastRetrievedTransactionsTimestamp,
            account.supportsRetrievingAccountTransactions, account.supportsRetrievingBalance,
            account.supportsTransferringMoney, account.supportsInstantPaymentMoneyTransfer
        )

        mappedAccount.id = account.technicalId
        mappedAccount.userSetDisplayName = account.userSetDisplayName

        mappedAccount.bookedTransactions = mapTransactions(account.bookedTransactions, mappedAccount)

        return mappedAccount
    }


    open fun mapBankAccounts(transactions: List<BankAccountEntity>, customer: Customer): List<BankAccount> {
        return transactions.map { mapBankAccount(it, customer) }
    }

    open fun mapBankAccount(account: BankAccountEntity, customer: Customer): BankAccount {
        val mappedAccount = BankAccount(
            customer, account.identifier, account.accountHolderName, account.iban, account.subAccountNumber,
            account.customerId, account.balance, account.currency, account.type, account.productName,
            account.accountLimit, account.lastRetrievedTransactionsTimestamp,
            account.supportsRetrievingAccountTransactions, account.supportsRetrievingBalance,
            account.supportsTransferringMoney, account.supportsInstantPaymentMoneyTransfer
        )

        mappedAccount.technicalId = account.id
        mappedAccount.userSetDisplayName = account.userSetDisplayName

        mappedAccount.bookedTransactions = mapTransactions(account.bookedTransactions, mappedAccount)

        return mappedAccount
    }


    open fun mapTransactions(transactions: List<AccountTransaction>, account: BankAccountEntity): List<AccountTransactionEntity> {
        return transactions.map { mapTransaction(it, account) }
    }

    open fun mapTransaction(transaction: AccountTransaction, account: BankAccountEntity): AccountTransactionEntity {
        return AccountTransactionEntity(account, transaction.amount, transaction.currency, transaction.unparsedUsage, transaction.bookingDate,
            transaction.otherPartyName, transaction.otherPartyBankCode, transaction.otherPartyAccountId, transaction.bookingText,
            transaction.valueDate, transaction.statementNumber, transaction.sequenceNumber, transaction.openingBalance, transaction.closingBalance,
            transaction.endToEndReference, transaction.customerReference, transaction.mandateReference, transaction.creditorIdentifier, transaction.originatorsIdentificationCode,
            transaction.compensationAmount, transaction.originalAmount, transaction.sepaUsage, transaction.deviantOriginator, transaction.deviantRecipient,
            transaction.usageWithNoSpecialType, transaction.primaNotaNumber, transaction.textKeySupplement, transaction.currencyType, transaction.bookingKey,
            transaction.referenceForTheAccountOwner, transaction.referenceOfTheAccountServicingInstitution, transaction.supplementaryDetails,
            transaction.transactionReferenceNumber, transaction.relatedReferenceNumber, transaction.technicalId)
    }


    open fun mapTransactions(transactions: List<AccountTransactionEntity>, account: BankAccount): List<AccountTransaction> {
        return transactions.map { mapTransaction(it, account) }
    }

    open fun mapTransaction(transaction: AccountTransactionEntity, account: BankAccount): AccountTransaction {
        val mappedTransaction = AccountTransaction(account, transaction.amount, transaction.currency, transaction.unparsedUsage, transaction.bookingDate,
            transaction.otherPartyName, transaction.otherPartyBankCode, transaction.otherPartyAccountId, transaction.bookingText,
            transaction.valueDate, transaction.statementNumber, transaction.sequenceNumber, transaction.openingBalance, transaction.closingBalance,
            transaction.endToEndReference, transaction.customerReference, transaction.mandateReference, transaction.creditorIdentifier, transaction.originatorsIdentificationCode,
            transaction.compensationAmount, transaction.originalAmount, transaction.sepaUsage, transaction.deviantOriginator, transaction.deviantRecipient,
            transaction.usageWithNoSpecialType, transaction.primaNotaNumber, transaction.textKeySupplement, transaction.currencyType, transaction.bookingKey,
            transaction.referenceForTheAccountOwner, transaction.referenceOfTheAccountServicingInstitution, transaction.supplementaryDetails,
            transaction.transactionReferenceNumber, transaction.relatedReferenceNumber)

        mappedTransaction.technicalId = transaction.id

        return mappedTransaction
    }

}