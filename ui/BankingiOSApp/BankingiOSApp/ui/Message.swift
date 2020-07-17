import Foundation
import SwiftUI


struct Message: Identifiable {
    let id = UUID()
    
    let title: Text
    
    let message: Text?
    
    let primaryButton: Alert.Button
    
    let secondaryButton: Alert.Button?
    
    
    init(title: Text = Text(""), message: Text? = nil, primaryButton: Alert.Button = .ok(), secondaryButton: Alert.Button? = nil) {
        self.title = title
        self.message = message
        
        self.primaryButton = primaryButton
        self.secondaryButton = secondaryButton
    }
    
    init(title: String = "", message: String? = nil, primaryButton: Alert.Button = .ok(), secondaryButton: Alert.Button? = nil) {
        self.title = Text(title)
        
        if let text = message {
            self.message = Text(text)
        }
        else {
            self.message = nil
        }
        
        self.primaryButton = primaryButton
        self.secondaryButton = secondaryButton
    }
    
}
