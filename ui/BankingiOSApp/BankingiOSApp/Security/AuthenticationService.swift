import SwiftUI


class AuthenticationService {
    
    static private let AuthenticationTypeUserDefaultsKey = "AuthenticationType"
    
    static private let KeychainAccountName = "LoginPassword"

    private let biometricAuthenticationService = BiometricAuthenticationService()
    
    
    
    var authenticationType: AuthenticationType {
        let authenticationTypeString = UserDefaults.standard.string(forKey: Self.AuthenticationTypeUserDefaultsKey, defaultValue: AuthenticationType.unset.rawValue)
        
        return AuthenticationType.init(rawValue: authenticationTypeString) ?? .unset
    }
    
    var needsAuthenticationToUnlockApp: Bool {
        let authenticationType = self.authenticationType
        
        return authenticationType != .unset && authenticationType != .none
    }
    
    var needsBiometricAuthenticationToUnlockApp: Bool {
        let authenticationType = self.authenticationType
        
        return authenticationType == .faceID || authenticationType == .touchID
    }
    
    var needsFaceIDToUnlockApp: Bool {
        return self.authenticationType == .faceID
    }
    
    var needsTouchIDToUnlockApp: Bool {
        return self.authenticationType == .touchID
    }
    
    var needsPasswordToUnlockApp: Bool {
        return self.authenticationType == .password
    }
    
    
    var deviceSupportsFaceID: Bool {
        return biometricAuthenticationService.isFaceIDSupported
    }
    
    var deviceSupportsTouchID: Bool {
        return biometricAuthenticationService.isTouchIDSupported
    }
    
    
    func setAuthenticationType(_ type: AuthenticationType) {
        if type != .unset { // it's not allowed to unset authentication type
            if needsPasswordToUnlockApp {
                deleteLoginPassword()
            }
            
            UserDefaults.standard.set(type.rawValue, forKey: Self.AuthenticationTypeUserDefaultsKey)
        }
        else {
            // TODO: what to do in this case, throw an exception?
        }
    }
    
    func setAuthenticationTypeToPassword(_ newPassword: String) {
        setAuthenticationType(.password)
        
        setLoginPassword(newPassword)
    }
    
    @discardableResult
    private func setLoginPassword(_ newPassword: String) -> Bool {
        do {
            let passwordItem = createKeychainPasswordItem()
            
            try passwordItem.savePassword(newPassword)
            
            return true
        } catch {
            NSLog("Could not save login password: \(error)")
        }
        
        return false
    }
    
    @discardableResult
    private func deleteLoginPassword() -> Bool {
        do {
            let passwordItem = createKeychainPasswordItem()
            
            try passwordItem.deleteItem()
            
            return true
        } catch {
            NSLog("Could not delete login password: \(error)")
        }
        
        return false
    }
    
    private func retrieveLoginPassword() -> String? {
        do {
            let passwordItem = createKeychainPasswordItem()
            
            return try passwordItem.readPassword()
        } catch {
            NSLog("Could not read login password: \(error)")
        }
        
        return nil
    }
    
    private func createKeychainPasswordItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.KeychainAccountName)
    }
    
    
    func loginWithBiometricAuthentication(_ authenticationResult: @escaping (Bool, String?) -> Void) {
        biometricAuthenticationService.authenticate("Authenticate with biometrics to unlock app reason", authenticationResult)
    }
    
    func loginWithPassword(_ enteredPassword: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        if retrieveLoginPassword() == enteredPassword {
            authenticationResult(true, nil)
        }
        else {
            authenticationResult(false, "Incorrect password entered".localize())
        }
    }
    
}
