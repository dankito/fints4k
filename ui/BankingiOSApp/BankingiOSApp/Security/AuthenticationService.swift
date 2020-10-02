import SwiftUI


class AuthenticationService {
    
    static private let AuthenticationTypeUserDefaultsKey = "AuthenticationType"
    
    static private let UserLoginPasswordKeychainAccountName = "UserLoginPassword"

    private let biometricAuthenticationService = BiometricAuthenticationService()
    
    
    
    var authenticationType: AuthenticationType {
        let authenticationTypeString = UserDefaults.standard.string(forKey: Self.AuthenticationTypeUserDefaultsKey, defaultValue: AuthenticationType.none.rawValue)

        return AuthenticationType.init(rawValue: authenticationTypeString) ?? .none
    }
    
    var needsAuthenticationToUnlockApp: Bool {
        let authenticationType = self.authenticationType
        
        return authenticationType != .none
    }
    
    var needsBiometricAuthenticationToUnlockApp: Bool {
        let authenticationType = self.authenticationType
        
        return authenticationType == .biometric
    }

    var needsFaceIDToUnlockApp: Bool {
        return self.needsBiometricAuthenticationToUnlockApp && self.deviceSupportsFaceID
    }

    var needsTouchIDToUnlockApp: Bool {
        return self.needsBiometricAuthenticationToUnlockApp && self.deviceSupportsTouchID
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
    
    
    func setAuthenticationMethodToPassword(_ newPassword: String) {
        setAuthenticationType(.password)
        
        setLoginPassword(newPassword)
    }
    
    func setAuthenticationMethodToBiometric() {
        setAuthenticationType(.biometric)
    }
    
    func removeAppProtection() {
        setAuthenticationType(.none)
    }
    
    private func setAuthenticationType(_ type: AuthenticationType) {
        if needsPasswordToUnlockApp {
            deleteLoginPassword()
        }
        
        UserDefaults.standard.set(type.rawValue, forKey: Self.AuthenticationTypeUserDefaultsKey)
    }
    
    func setAuthenticationTypeToPassword(_ newPassword: String) {
        setAuthenticationType(.password)
        
        setLoginPassword(newPassword)
    }
    
    @discardableResult
    private func setLoginPassword(_ newPassword: String) -> Bool {
        do {
            let passwordItem = createUserLoginPasswordKeychainItem()
            
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
            let passwordItem = createUserLoginPasswordKeychainItem()
            
            try passwordItem.deleteItem()
            
            return true
        } catch {
            NSLog("Could not delete login password: \(error)")
        }
        
        return false
    }
    
    private func retrieveLoginPassword() -> String? {
        do {
            let passwordItem = createUserLoginPasswordKeychainItem()
            
            return try passwordItem.readPassword()
        } catch {
            NSLog("Could not read login password: \(error)")
        }
        
        return nil
    }
    
    private func createUserLoginPasswordKeychainItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.UserLoginPasswordKeychainAccountName)
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
