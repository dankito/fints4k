import Foundation
import CoreData
import UIKit
import BankingUiSwift


class CoreDataBankingPersistence: IBankingPersistence, IRemitteeSearcher {
    
    private let mapper = Mapper()
    
    private let context: NSManagedObjectContext
    
    
    init(context: NSManagedObjectContext) {
        self.context = context
        
        // to fix merging / updating cached objects, see Mapper
        context.mergePolicy = NSMergePolicy(merge: NSMergePolicyType.mergeByPropertyObjectTrumpMergePolicyType)
    }
    
    
    func saveOrUpdateAccount(customer: Customer, allCustomers: [Customer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            if customer.technicalId.isCoreDataId == false { // an unpersisted bank (but check should not be necessary)
                context.insert(mapped)
            }
            
            try context.save()
            
            setIds(customer, mapped)
        } catch {
            NSLog("Could not save Customer \(customer): \(error)")
        }
    }
    
    private func setIds(_ customer: Customer, _ mappedCustomer: PersistedCustomer) {
        customer.technicalId = mappedCustomer.objectIDAsString
        
        for account in customer.accounts {
            if let mappedAccount = mappedCustomer.accounts?.first { ($0 as! PersistedBankAccount).identifier == account.identifier} as? PersistedBankAccount {
                account.technicalId = mappedAccount.objectIDAsString
            }
        }
        
        for tanProcedure in customer.supportedTanProcedures {
            if let mappedTanProcedure = mappedCustomer.supportedTanProcedures?.first { ($0 as! PersistedTanProcedure).bankInternalProcedureCode == tanProcedure.bankInternalProcedureCode } as? PersistedTanProcedure {
                tanProcedure.technicalId = mappedTanProcedure.objectIDAsString
            }
        }
    }
    
    
    func readPersistedAccounts_() -> [Customer] {
        var customers: [PersistedCustomer] = []
        
        do {
            let request: NSFetchRequest<PersistedCustomer> = PersistedCustomer.fetchRequest()
            request.returnsObjectsAsFaults = false
            
            try customers = context.fetch(request)
        } catch {
            NSLog("Could not request Customers: \(error)")
        }
        
        return customers.map( { mapper.map($0) } )
    }
    
    func deleteAccount(customer: Customer, allCustomers: [Customer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            context.delete(mapped)
            
            try context.save()
        } catch {
            NSLog("Could not delete Customer \(customer): \(error)")
        }
    }
    
    func saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: [AccountTransaction]) {
        if let persistedAccount = context.objectByID(bankAccount.technicalId) as? PersistedBankAccount {
            for transaction in transactions {
                if transaction.technicalId.isCoreDataId == false { // TODO: or also update already persisted transactions?
                    do {
                        let mappedTransaction = mapper.map(persistedAccount, transaction, context)
                        
                        try context.save()
                        
                        transaction.technicalId = mappedTransaction.objectIDAsString
                    } catch {
                        NSLog("Could not save transaction \(transaction.transactionIdentifier) of account \(bankAccount.displayName): \(error)")
                    }
                }
            }
        }
    }
    
    
    func saveUrlToFile(url: String, file: URL) {
        if let remoteUrl = URL.encoded(url) {
            if let fileData = try? Data(contentsOf: remoteUrl) {
                do {
                    try UIImage(data: fileData)?.pngData()?.write(to: file)
                } catch {
                    NSLog("Could not save url '\(url)' to file '\(file): \(error)")
                }
                
                // not indented for this kind of data but at least it works
                UserDefaults.standard.set(fileData, forKey: file.absoluteString)
            }
        }
    }
    
    func readContentOfFile(_ filePath: String) -> Data? {
        return UserDefaults.standard.data(forKey: filePath)
    }
    
    func findRemittees(query: String) -> [Remittee] {
        var transactions: [PersistedAccountTransaction] = []
        
        do {
            let request: NSFetchRequest<PersistedAccountTransaction> = PersistedAccountTransaction.fetchRequest()
            request.returnsObjectsAsFaults = false
            
            request.predicate = NSPredicate(format: "otherPartyName CONTAINS[c] %@", query)
            
            request.propertiesToFetch = [ "otherPartyName", "otherPartyBankCode", "otherPartyAccountId" ]
            request.sortDescriptors = [ NSSortDescriptor(key: "otherPartyName", ascending: true) ]
            
            try transactions = context.fetch(request)
        } catch {
            NSLog("Could not request Customers: \(error)")
        }
        
        let remittees = transactions.map( { Remittee(name: $0.otherPartyName ?? "", iban: $0.otherPartyAccountId, bic: $0.otherPartyBankCode, bankName: nil) } )
        
        let uniqueRemittees = Set<Remittee>(remittees)
        
        return Array(uniqueRemittees)
    }
    
    
    func deleteAll() {
        do {
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "PersistedCustomer")
            
            let deleteAll = NSBatchDeleteRequest(fetchRequest: request)
            
            try context.execute(deleteAll)
        } catch {
            NSLog("Could not delete all Customers: \(error)")
        }
    }
    
}
