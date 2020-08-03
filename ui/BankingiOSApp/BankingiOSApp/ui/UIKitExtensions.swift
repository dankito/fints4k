import SwiftUI


extension UIResponder {
    
    @discardableResult func focus() -> Bool {
        return becomeFirstResponder()
    }
    
    @discardableResult func clearFocus() -> Bool {
        return resignFirstResponder()
    }
    
}
