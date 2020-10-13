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
        else { // first app run, no authentication type persisted yet -> set to .unprotected
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
    
    var supportedBiometricAuthenticationLocalizedName: String {
        if deviceSupportsTouchID {
            return "TouchID".localize()
        }
        else {
            return "FaceID".localize()
        }
    }
    
    
    func setAuthenticationMethodToPassword(_ newLoginPassword: String) {
        setAuthenticationType(.password)
        
        setPasswords(false, newLoginPassword)
    }
    
    func setAuthenticationMethodToBiometric() {
        setAuthenticationType(.biometric)

        setPasswords(true, nil)
    }
    
    func removeAppProtection() {
        setAuthenticationType(.none)

        setPasswords(false, nil)
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
    private func setPasswords(_ useBiometricAuthentication: Bool, _ newLoginPassword: String?) -> Bool {
        do {
            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            let currentPassword = try? passwordItem.readPassword()
            
            try? passwordItem.deleteItem()
            
            var databasePassword = currentPassword ?? ""
            
            if let currentPassword = currentPassword {
                try passwordItem.savePassword(currentPassword)
            }
            else {
                if let newDefaultPassword = createNewDefaultPassword(useBiometricAuthentication) {
                    databasePassword = newDefaultPassword
                }
            }
            
            if let newLoginPassword = newLoginPassword {
                setLoginPassword(newLoginPassword)
                databasePassword = newLoginPassword + "_" + databasePassword
            }
            
            return true
        } catch {
            NSLog("Could not save default password: \(error)")
        }
        
        return false
    }
    
    @discardableResult
    private func createNewDefaultPassword(_ useBiometricAuthentication: Bool) -> String? {
        do {
            let newDefaultPassword = generateRandomPassword(30)

            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            try passwordItem.savePassword(newDefaultPassword)
            
            return newDefaultPassword
        } catch {
            NSLog("Could not create new default password: \(error)")
        }
        
        return nil
    }
    
    private func createDefaultPasswordKeychainItem(_ useBiometricAuthentication: Bool) -> KeychainPasswordItem {
        var accessControl: SecAccessControl? = nil
        var context: LAContext? = nil
        
        if useBiometricAuthentication {
            accessControl = SecAccessControlCreateWithFlags(nil, // Use the default allocator.
                                                            kSecAttrAccessibleWhenUnlocked,
                                                            .userPresence,
                                                            nil) // Ignore any error.
            
            context = LAContext()
            context?.touchIDAuthenticationAllowableReuseDuration = 45
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
    
    
    func authenticateUserWithBiometric(_ prompt: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        biometricAuthenticationService.authenticate(prompt, authenticationResult)
    }
    
    func authenticateUserWithPassword(_ enteredPassword: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
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
