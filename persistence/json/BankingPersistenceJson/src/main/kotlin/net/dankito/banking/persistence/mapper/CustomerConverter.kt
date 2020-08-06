package net.dankito.banking.persistence.mapper

import net.dankito.banking.persistence.model.AccountTransactionEntity
import net.dankito.banking.persistence.model.BankAccountEntity
import net.dankito.banking.persistence.model.CustomerEntity
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.Customer
import org.mapstruct.*


@Mapper
abstract class CustomerConverter {

    // Context is needed to fix cycle dependencies issue

    protected val bankAccountCustomerField = BankAccount::class.java.getDeclaredField(BankAccount::customer.name)


    init {
        bankAccountCustomerField.isAccessible = true
    }


    @Mapping(source = "technicalId", target = "id")
    abstract fun mapToEntity(customer: Customer, @Context context: CycleAvoidingMappingContext): CustomerEntity

    @InheritInverseConfiguration
    abstract fun mapCustomer(customer: CustomerEntity, @Context context: CycleAvoidingMappingContext): Customer

    abstract fun mapCustomers(customers: List<Customer>, @Context context: CycleAvoidingMappingContext): List<CustomerEntity>

    open fun mapCustomers(customers: List<Customer>): List<CustomerEntity> {
        // create a new context instance each time as otherwise just cached instance would be taken und BankAccounts and AccountTransactions would never get updated
        return mapCustomers(customers, CycleAvoidingMappingContext())
    }

    abstract fun mapCustomerEntities(customers: List<CustomerEntity>, @Context context: CycleAvoidingMappingContext): List<Customer>

    open fun mapCustomerEntities(customers: List<CustomerEntity>): List<Customer> {
        // create a new context instance each time as otherwise just cached instance would be taken und BankAccounts and AccountTransactions would never get updated
        return mapCustomerEntities(customers, CycleAvoidingMappingContext())
    }


    @Mapping(source = "technicalId", target = "id")
    abstract fun mapBankAccount(account: BankAccount, @Context context: CycleAvoidingMappingContext): BankAccountEntity

    @InheritInverseConfiguration
    abstract fun mapBankAccount(account: BankAccountEntity, @Context context: CycleAvoidingMappingContext): BankAccount

    abstract fun mapBankAccounts(accounts: List<BankAccount>, @Context context: CycleAvoidingMappingContext): List<BankAccountEntity>

    abstract fun mapBankAccountEntities(accounts: List<BankAccountEntity>, @Context context: CycleAvoidingMappingContext): List<BankAccount>

    @AfterMapping
    open fun mapBankAccountCustomer(serializedAccount: BankAccountEntity, @MappingTarget account: BankAccount, @Context context: CycleAvoidingMappingContext) {
        val mappedCustomer = mapCustomer(serializedAccount.customer, context)

        bankAccountCustomerField.set(account, mappedCustomer)
    }



    @Mapping(source = "technicalId", target = "id")
    abstract fun mapTransaction(transaction: AccountTransaction, @Context context: CycleAvoidingMappingContext): AccountTransactionEntity

    @InheritInverseConfiguration
    fun mapTransaction(transaction: AccountTransactionEntity, @Context context: CycleAvoidingMappingContext): AccountTransaction {
        val account = mapBankAccount(transaction.bankAccount, context)

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

    abstract fun mapTransactions(transactions: List<AccountTransaction>, @Context context: CycleAvoidingMappingContext): List<AccountTransactionEntity>

    abstract fun mapTransactionEntities(transactions: List<AccountTransactionEntity>, @Context context: CycleAvoidingMappingContext): List<AccountTransaction>

}