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


extension UIDevice {
    
    static var deviceType: UIUserInterfaceIdiom {
        UIDevice.current.deviceType
    }
    
    var deviceType: UIUserInterfaceIdiom {
        self.userInterfaceIdiom
    }
    
    
    static var isRunningOniPad: Bool {
        UIDevice.current.userInterfaceIdiom == .pad
    }
    
    var isRunningOniPad: Bool {
        self.userInterfaceIdiom == .pad
    }
    
}


extension UIAlertAction {
    
    static func ok(_ handler: (() -> Void)? = nil) -> UIAlertAction {
        return `default`("OK", handler)
    }
    
    static func `default`(_ title: String, _ handler: (() -> Void)? = nil) -> UIAlertAction {
        return UIAlertAction(title, .default, handler)
    }
    
    static func destructive(_ title: String, _ handler: (() -> Void)? = nil) -> UIAlertAction {
        return UIAlertAction(title, .destructive, handler)
    }
    
    static func cancel(_ handler: (() -> Void)? = nil) -> UIAlertAction {
        return cancel("Cancel", handler)
    }
    
    static func cancel(_ title: String, _ handler: (() -> Void)? = nil) -> UIAlertAction {
        return UIAlertAction(title, .cancel, handler)
    }
    
    convenience init(_ title: String, _ style: UIAlertAction.Style = .default, _ handler: (() -> Void)? = nil) {
        self.init(title: title.localize(), style: style, handler: { _ in handler?() })
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
