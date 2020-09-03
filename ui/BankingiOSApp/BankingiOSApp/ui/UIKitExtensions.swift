import SwiftUI


extension UIApplication {
    
    static func hideKeyboard() {
        shared.hideKeyboard()
    }
    
    func hideKeyboard() {
        sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
    
}


extension UIResponder {
    
    @discardableResult func focus() -> Bool {
        return becomeFirstResponder()
    }
    
    @discardableResult func clearFocus() -> Bool {
        return resignFirstResponder()
    }
    
}

extension UserDefaults {
    
    func string(forKey key: String, defaultValue: String) -> String {
        return UserDefaults.standard.object(forKey: key) as? String ?? defaultValue
    }
    
    func integer(forKey key: String, defaultValue: Int) -> Int {
        return UserDefaults.standard.object(forKey: key) as? Int ?? defaultValue
    }
    
    func float(forKey key: String, defaultValue: Float) -> Float {
        return UserDefaults.standard.object(forKey: key) as? Float ?? defaultValue
    }
    
    func double(forKey key: String, defaultValue: Double) -> Double {
        return UserDefaults.standard.object(forKey: key) as? Double ?? defaultValue
    }
    
    func bool(forKey key: String, defaultValue: Bool) -> Bool {
        return UserDefaults.standard.object(forKey: key) as? Bool ?? defaultValue
    }
    
}
