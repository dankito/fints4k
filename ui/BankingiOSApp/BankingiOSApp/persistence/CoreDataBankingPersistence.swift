import Foundation
import CoreData
import BankingUiSwift


class CoreDataBankingPersistence: BUCIBankingPersistence {
    
    private let persistentContainer: NSPersistentContainer
    
    private let mapper = Mapper()
    
    private var context: NSManagedObjectContext {
        return persistentContainer.viewContext
    }
    
    
    init(persistentContainer: NSPersistentContainer) {
        self.persistentContainer = persistentContainer
    }
    
    
    func saveOrUpdateAccount(customer: BUCCustomer, allCustomers: [BUCCustomer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            context.insert(mapped)
            
            try context.save()
        } catch {
            print("Could not save Customer \(customer): \(error)")
        }
    }
    
    func readPersistedAccounts_() -> [BUCCustomer] {
        var customers: [Customer] = []
        
        do {
            let request: NSFetchRequest<Customer> = Customer.fetchRequest()
            request.returnsObjectsAsFaults = false
            
            try customers = context.fetch(request)
        } catch {
            NSLog("Could not request Customers: \(error)")
        }
        
        return customers.map( { mapper.map($0) } )
    }
    
    func deleteAccount(customer: BUCCustomer, allCustomers: [BUCCustomer]) {
        do {
            let mapped = mapper.map(customer, context)
            
            context.delete(mapped)
            
            try context.save()
        } catch {
            NSLog("Could not delete Customer \(customer): \(error)")
        }
    }
    
    func saveOrUpdateAccountTransactions(bankAccount: BUCBankAccount, transactions: [BUCAccountTransaction]) {
        // TODO
    }
    
    func saveUrlToFile(url: String, file: URL) {
        // TODO
    }
    
    
    func deleteAll() {
        do {
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Customer")
            
            let deleteAll = NSBatchDeleteRequest(fetchRequest: request)
            
            try context.execute(deleteAll)
        } catch {
            NSLog("Could not delete all Customers: \(error)")
        }
    }
    
}
