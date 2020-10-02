import SwiftUI
import LocalAuthentication


class AuthenticationService {
    
    static private let AuthenticationTypeKeychainAccountName = "AuthenticationType"

    static private let DefaultPasswordKeychainAccountName = "DefaultPassword"
    
    static private let UserLoginPasswordKeychainAccountName = "UserLoginPassword"

    private let biometricAuthenticationService = BiometricAuthenticationService()
    
    
    init() {
        if let type = readAuthenticationType() {
            self.authenticationType = type
        }
        else {
            removeAppProtection()
        }
    }
    
    
    private (set) var authenticationType: AuthenticationType = .none
    
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
        setDefaultPassword(false)
    }
    
    func setAuthenticationMethodToBiometric() {
        setAuthenticationType(.biometric)

        setDefaultPassword(true)
    }
    
    func removeAppProtection() {
        setAuthenticationType(.none)

        setDefaultPassword(false)
    }
    
    
    private func readAuthenticationType() -> AuthenticationType? {
        do {
            let authenticationTypeItem = createAuthenticationTypeKeychainItem()
            
            let authenticationTypeString = try authenticationTypeItem.readPassword()

            return AuthenticationType.init(rawValue: authenticationTypeString)
        } catch {
            NSLog("Could not read AuthenticationType: \(error)")
        }
        
        return nil
    }
    
    private func setAuthenticationType(_ type: AuthenticationType) {
        if needsPasswordToUnlockApp {
            deleteLoginPassword()
        }
        
        do {
            let authenticationTypeItem = createAuthenticationTypeKeychainItem()
            
            try authenticationTypeItem.savePassword(type.rawValue)
        } catch {
            NSLog("Could not save AuthenticationType: \(error)")
        }
        
        self.authenticationType = type
    }
    
    private func createAuthenticationTypeKeychainItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.AuthenticationTypeKeychainAccountName)
    }
    
    
    @discardableResult
    private func setDefaultPassword(_ useBiometricAuthentication: Bool) -> Bool {
        do {
            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            let currentPassword = try? passwordItem.readPassword()
            
            try? passwordItem.deleteItem()
            
            if let currentPassword = currentPassword {
                try passwordItem.savePassword(currentPassword)
            }
            else {
                createNewDefaultPassword(useBiometricAuthentication)
            }
            
            return true
        } catch {
            NSLog("Could not save default password: \(error)")
        }
        
        return false
    }
    
    private func createNewDefaultPassword(_ useBiometricAuthentication: Bool) {
        do {
            let newDefaultPassword = generateRandomPassword(30)

            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            try passwordItem.savePassword(newDefaultPassword)
        } catch {
            NSLog("Could not create new default password: \(error)")
        }
    }
    
    private func createDefaultPasswordKeychainItem(_ useBiometricAuthentication: Bool) -> KeychainPasswordItem {
        var accessControl: SecAccessControl? = nil
        var context: LAContext? = nil
        
        if useBiometricAuthentication {
            accessControl = SecAccessControlCreateWithFlags(nil, // Use the default allocator.
                                                            kSecAttrAccessibleWhenUnlocked,
                                                            .userPresence,
                                                            nil) // Ignore any error.
            
            // TODO: this does not work yet, setting LAContext results in a "unexpectedPasswordData" error
//            context = LAContext()
//            context?.touchIDAuthenticationAllowableReuseDuration = 45
        }
        
        return KeychainPasswordItem(service: Self.DefaultPasswordKeychainAccountName, account: nil, accessGroup: nil, secAccessControl: accessControl, authenticationContext: context)
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
    
    
    private func generateRandomPassword(_ passwordLength: Int) -> String {
        let dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789§±!@#$%^&*-_=+;:|/?.>,<"
        
        return String((0 ..< passwordLength).map{ _ in dictionary.randomElement()! })
    }
    
}
