import SwiftUI
import BankingUiSwift


extension Message {
    
    static func createUnsavedChangesMessage(_ discardChanges: @escaping () -> Void) -> Message {
        return Message(title: Text("Unsaved changes"),
                       message: Text("Changed data hasn't been saved. Are you sure you want to discard them?"),
                       primaryButton: .discard(discardChanges),
                       secondaryButton: .cancel())
    }
    
    static func createAskUserToDeleteAccountMessage(_ bank: ICustomer, _ deleteAccount: @escaping (ICustomer) -> Void) -> Message {
        return Message(title: Text("Really delete account '\(bank.displayName)'?"),
                       message: Text("All data for this account will be permanently deleted locally."),
                       primaryButton: .destructive(Text("Delete"), action: { deleteAccount(bank) } ),
                       secondaryButton: .cancel())
    }
    
}
