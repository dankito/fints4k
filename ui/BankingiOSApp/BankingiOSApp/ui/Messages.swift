import SwiftUI
import BankingUiSwift


extension Message {
    
    static func createUnsavedChangesMessage(_ discardChanges: @escaping () -> Void) -> Message {
        return Message(title: Text("Discard changes?"),
                       message: Text("Your changes have not been saved. Are you sure you want to discard them?"),
                       primaryButton: .discard(discardChanges),
                       secondaryButton: .cancel())
    }
    
    static func createAskUserToDeleteAccountMessage(_ bank: IBankData, _ deleteAccount: @escaping (IBankData) -> Void) -> Message {
        return Message(title: Text("Delete account?"),
                       message: Text("Would you like to remove account '\(bank.displayName)' from app?"),
                       primaryButton: .destructive(Text("Delete"), action: { deleteAccount(bank) } ),
                       secondaryButton: .cancel())
    }
    
}
