import SwiftUI
import LocalAuthentication
import CryptoSwift
import BankingUiSwift


class AuthenticationService {
    
    static private let AuthenticationTypeKeychainAccountName = "AuthenticationType"

    static private let DefaultPasswordKeychainAccountName = "DefaultPassword"
    
    static private let UserLoginPasswordKeychainAccountName = "UserLoginPassword"
    
    static private let UserLoginPasswordSaltKeychainAccountName = "UserLoginPasswordSalt"
    
    static private let Key = ">p(Z5&RRA,@_+W0#" // length = 16 // TODO: find a better way to store key
    
    static private let IV = "drowssapdrowssap" // length = 16
    

    private let biometricAuthenticationService = BiometricAuthenticationService()
    
    private let persistence: IBankingPersistence
    
    
    init(_ persistence: IBankingPersistence) {
        self.persistence = persistence
        
        if UserDefaults.standard.bool(forKey: "hasAppBeenStartedBefore", defaultValue: false) == false { // when uninstalling app key chain items aren't deleted -> delete them after reinstall
            deleteAllKeyChainItems()
            UserDefaults.standard.setValue(true, forKey: "hasAppBeenStartedBefore")
        }
        
        if let type = readAuthenticationType() {
            self.authenticationType = type
            
            if type == .none {
                openDatabase(false, nil)
            }
        }
        else { // first app run, no authentication type persisted yet -> set default password
            removeAppProtection()
            openDatabase(false, nil)
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
    
    
    func loginUserWithPassword(_ enteredPassword: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        if let storedHash = readLoginPasswordHash() {
            if let salt = readLoginPasswordSalt() {
                if let hashOfEnteredPassword = hashLoginPassword(enteredPassword, salt) {
                    if storedHash == hashOfEnteredPassword {
                        let decryptDatabaseResult = openDatabase(false, enteredPassword)
                        authenticationResult(decryptDatabaseResult, nil)
                        
                        return
                    }
                }
            }
        }
        
        authenticationResult(false, "Incorrect password entered".localize())
    }
    
    func loginUserWithBiometric(_ prompt: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        authenticateUserWithBiometric(prompt) { successful, error in
            var decryptDatabaseResult = false
            if successful {
                decryptDatabaseResult = self.openDatabase(true, nil)
            }
            
            authenticationResult(successful && decryptDatabaseResult, error)
        }
    }
    
    func authenticateUserWithBiometricToSetAsNewAuthenticationMethod(_ prompt: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        authenticateUserWithBiometric(prompt, authenticationResult)
    }
    
    private func authenticateUserWithBiometric(_ prompt: String, _ authenticationResult: @escaping (Bool, String?) -> Void) {
        biometricAuthenticationService.authenticate(prompt) { successful, error in
            authenticationResult(successful, error)
        }
    }
    
    @discardableResult
    private func openDatabase(_ useBiometricAuthentication: Bool, _ userLoginPassword: String?) -> Bool {
        if var databasePassword = readDefaultPassword(useBiometricAuthentication) {
            if let loginPassword = userLoginPassword {
                databasePassword = concatPasswords(loginPassword, databasePassword)
            }

            return persistence.decryptData(password: map(databasePassword))
        }
        
        return false
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
            
            if let authenticationTypeString = try decrypt(authenticationTypeItem.readPassword()) {
                return AuthenticationType.init(rawValue: authenticationTypeString)
            }
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
            if let encrypted = encrypt(type.rawValue) {
                let authenticationTypeItem = createAuthenticationTypeKeychainItem()
                
                try authenticationTypeItem.savePassword(encrypted)
            }
        } catch {
            NSLog("Could not save AuthenticationType: \(error)")
        }
        
        self.authenticationType = type
    }
    
    private func deleteAuthenticationTypeKeychainItem() {
        do {
            let item = createAuthenticationTypeKeychainItem()
            
            try item.deleteItem()
        } catch {
            NSLog("Could not delete authentication type keychain item: \(error)")
        }
    }
    
    private func createAuthenticationTypeKeychainItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.AuthenticationTypeKeychainAccountName)
    }
    
    
    @discardableResult
    private func setPasswords(_ useBiometricAuthentication: Bool, _ newLoginPassword: String?) -> Bool {
        deleteDefaultPassword(useBiometricAuthentication) // TODO: needed?

        var databasePassword = ""
        
        if let newDefaultPassword = createAndSetDefaultPassword(useBiometricAuthentication) {
            databasePassword = newDefaultPassword
        }
        
        if let newLoginPassword = newLoginPassword {
            setLoginPassword(newLoginPassword)
            databasePassword = concatPasswords(newLoginPassword, databasePassword)
        }

        return persistence.changePassword(newPassword: map(databasePassword))
    }
    
    @discardableResult
    private func createAndSetDefaultPassword(_ useBiometricAuthentication: Bool) -> String? {
        do {
            let newDefaultPassword = generateRandomPassword(30)

            if let encrypedNewDefaultPassword = encrypt(newDefaultPassword) {
                let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
                
                try passwordItem.savePassword(encrypedNewDefaultPassword)
                
                return newDefaultPassword
            }
        } catch {
            NSLog("Could not create new default password: \(error)")
        }
        
        return nil
    }
    
    private func readDefaultPassword(_ useBiometricAuthentication: Bool) -> String? {
        do {
            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            return try decrypt(passwordItem.readPassword())
        } catch {
            NSLog("Could not read default password: \(error)")
        }
        
        return nil
    }
    
    @discardableResult
    private func deleteDefaultPassword(_ useBiometricAuthentication: Bool) -> Bool {
        do {
            let passwordItem = createDefaultPasswordKeychainItem(useBiometricAuthentication)
            
            return deleteDefaultPassword(passwordItem)
        } catch {
            NSLog("Could not delete default password: \(error)")
        }
        
        return false
    }
    
    @discardableResult
    private func deleteDefaultPassword(_ passwordItem: KeychainPasswordItem) -> Bool {
        do {
            try? passwordItem.deleteItem()
            
            return true
        } catch {
            NSLog("Could not delete default password: \(error)")
        }
        
        return false
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
            let salt = Array(generateRandomPassword(8).utf8)
            
            if let passwordHash = hashLoginPassword(newPassword, salt) {
                if saveLoginPasswordSalt(salt) {
                    let passwordItem = createUserLoginPasswordKeychainItem()
                    
                    try passwordItem.savePassword(passwordHash)
                    
                    return true
                }
            }
        } catch {
            NSLog("Could not save login password: \(error)")
        }
        
        return false
    }
    
    private func readLoginPasswordHash() -> String? {
        do {
            let passwordItem = createUserLoginPasswordKeychainItem()
            
            return try passwordItem.readPassword()
        } catch {
            NSLog("Could not read login password: \(error)")
        }
        
        return nil
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
    
    private func createUserLoginPasswordKeychainItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.UserLoginPasswordKeychainAccountName)
    }
    
    
    @discardableResult
    private func saveLoginPasswordSalt(_ salt: Array<UInt8>) -> Bool {
        do {
            let saltItem = createUserLoginPasswordSaltKeychainItem()
            
            if let saltBase64Encoded = salt.toBase64() {
                try saltItem.savePassword(saltBase64Encoded)
                
                return true
            }
        } catch {
            NSLog("Could not save login password salt: \(error)")
        }
        
        return false
    }
    
    private func readLoginPasswordSalt() -> Array<UInt8>? {
        do {
            let saltItem = createUserLoginPasswordSaltKeychainItem()
            
            let saltBase64Encoded = try saltItem.readPassword()
            
            return Array<UInt8>(base64: saltBase64Encoded)
        } catch {
            NSLog("Could not read login password salt: \(error)")
        }
        
        return nil
    }
    
    @discardableResult
    private func deleteLoginPasswordSalt() -> Bool {
        do {
            let saltItem = createUserLoginPasswordSaltKeychainItem()
            
            try saltItem.deleteItem()
            
            return true
        } catch {
            NSLog("Could not delete login password salt: \(error)")
        }
        
        return false
    }
    
    private func createUserLoginPasswordSaltKeychainItem() -> KeychainPasswordItem {
        return KeychainPasswordItem(Self.UserLoginPasswordSaltKeychainAccountName)
    }
    
    
    private func deleteAllKeyChainItems() {
        deleteAuthenticationTypeKeychainItem()
        
        deleteDefaultPassword(false) // TODO: which boolean value to set here? does it make any difference if it comes to deleting the key chain item?a
        deleteDefaultPassword(true)
        
        deleteLoginPassword()
        
        deleteLoginPasswordSalt()
    }
    
    
    private func generateRandomPassword(_ passwordLength: Int) -> String {
        let dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789§±!@#$%^&*-_=+;:|/?.>,<"
        
        return String((0 ..< passwordLength).map{ _ in dictionary.randomElement()! })
    }
    
    
    private func encrypt(_ string: String) -> String? {
        do {
            let cipher = try getCipher()
            
            let cipherText = try cipher.encrypt(Array(string.utf8))
            
            return cipherText.toBase64()
        } catch {
            NSLog("Could not encrypt value: \(error)")
        }
        
        return nil
    }
    
    private func decrypt(_ base64EncodedCipherText: String) -> String? {
        do {
            let bytes = Array<UInt8>(base64: base64EncodedCipherText)
            
            let cipher = try getCipher()
            
            let decryptedBytes = try cipher.decrypt(bytes)
            
            return String(bytes: decryptedBytes, encoding: .utf8)
        } catch {
            NSLog("Could not decrypt cipher text: \(error)")
        }
        
        return nil
    }
    
    private func getCipher() throws -> AES {
        return try AES(key: Self.Key, iv: Self.IV)
    }
    
    
    private func hashLoginPassword(_ loginPassword: String, _ salt: Array<UInt8>) -> String? {
        do {
            let password = Array(loginPassword.utf8)
        
            let bytes = try Scrypt(password: password, salt: salt, dkLen: 64, N: 256, r: 8, p: 1).calculate()
            
            return bytes.toBase64()
        } catch {
            NSLog("Could not create hash for login password: \(error)")
        }
        
        return nil
    }
    
    private func concatPasswords(_ loginPassword: String, _ defaultPassword: String) -> String {
        return loginPassword + "_" + defaultPassword
    }
    
    private func map(_ string: String) -> KotlinCharArray {
        let array = KotlinCharArray(size: Int32(string.count))
        
        for i in 0 ..< string.count {
            array.set(index: Int32(i), value: (string as NSString).character(at: i))
        }
        
        return array
    }
    
}
