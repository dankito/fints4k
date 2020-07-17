import Foundation
import SwiftUI


struct Message: Identifiable {
    let id = UUID()
    
    let title: Text
    
    let message: Text?
    
    
    init(title: Text = Text(""), message: Text? = nil) {
        self.title = title
        self.message = message
    }
    
    init(title: String = "", message: String? = nil) {
        self.title = Text(title)
        
        if let text = message {
            self.message = Text(text)
        }
        else {
            self.message = nil
        }
    }
    
}
