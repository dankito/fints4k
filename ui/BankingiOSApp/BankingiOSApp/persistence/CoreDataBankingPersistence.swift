import Foundation
import CoreData
import BankingUiSwift


class CoreDataBankingPersistence: IBankingPersistence, IRemitteeSearcher {

    private let persistentContainer: NSPersistentContainer
    
    private let mapper = Mapper()
    
    private var context: NSManagedObjectContext {
        return persistentContainer.viewContext
    }
    
    
    init(persistentContainer: NSPersistentContainer) {
        self.persistentContainer = persistentContainer
    }
    
    
    func saveOrUpdateAccount(customer: Customer, allCustomers: [Customer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            context.insert(mapped)
            
            try context.save()
        } catch {
            print("Could not save Customer \(customer): \(error)")
        }
    }
    
    func readPersistedAccounts_() -> [Customer] {
        var customers: [PersistedCustomer] = []
        
        do {
            let request: NSFetchRequest<PersistedCustomer> = PersistedCustomer.fetchRequest()
            request.returnsObjectsAsFaults = false
            
            try customers = context.fetch(request)
        } catch {
            print("Could not request Customers: \(error)")
        }
        
        return customers.map( { mapper.map($0) } )
    }
    
    func deleteAccount(customer: Customer, allCustomers: [Customer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            context.delete(mapped)
            
            try context.save()
        } catch {
            print("Could not delete Customer \(customer): \(error)")
        }
    }
    
    func saveOrUpdateAccountTransactions(bankAccount: BankAccount, transactions: [AccountTransaction]) {
        do {
            let mapped = mapper.map(bankAccount.customer, context)
            
            context.insert(mapped)
            
            try context.save()
        } catch {
            print("Could not save transactions of account \(bankAccount): \(error)")
        }
    }
    
    func saveUrlToFile(url: String, file: URL) {
        // TODO
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
