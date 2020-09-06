import Foundation
import SwiftUI
import BankingUiSwift


extension AppDelegate {

    public static var current: AppDelegate { UIApplication.shared.delegate as! AppDelegate }

}

extension SceneDelegate {

    public static var currentWindow: UIWindow {
        UIApplication.shared.windows.first(where: { (window) -> Bool in window.isKeyWindow})!
    }

    public static var currentScene: UIWindowScene { currentWindow.windowScene! }
    
    public static var current: SceneDelegate { currentScene.delegate as! SceneDelegate }
    
    public static var rootViewController: UIViewController? {
        currentWindow.rootViewController
    }
    
    public static var rootNavigationController: UINavigationController? {
        rootViewController as? UINavigationController
    }
    
    public static var rootTabBarController: UITabBarController? {
        rootNavigationController?.viewControllers.first as? UITabBarController
    }
    
    public static var currentViewController: UIViewController? {
        var currentViewController = rootTabBarController?.selectedViewController ?? rootTabBarController
        
        while currentViewController?.presentedViewController != nil {
            currentViewController = currentViewController?.presentedViewController
        }
        
        return currentViewController
    }
    
    public static var currentNavigationItem: UINavigationItem? {
        currentViewController?.navigationItem
    }
    
    
    public static func navigateToView<Content: View>(_ view: Content) {
        navigateToViewController(UIHostingController(rootView: view))
    }
    
    public static func navigateToViewController(_ viewController: UIViewController) {
        rootNavigationController?.pushViewController(viewController, animated: true)
    }

}


extension KeychainPasswordItem {
    
    init(_ accountName: String) {
        self.init(service: "Bankmeister", account: accountName, accessGroup: nil)
    }
    
}


extension Customer : Identifiable {

    public var id: UUID { UUID() }
    
}

extension BankAccount : Identifiable {

    public var id: UUID { UUID() }
    
}

extension AccountTransaction : Identifiable {

    public var id: UUID { UUID() }
    
}


extension Array where Element == Customer {
    
    func sumBalances() -> CommonBigDecimal {
        return CommonBigDecimal(decimal_: self.map { $0.balance.decimal }.sum())
    }
    
}

extension Array where Element == AccountTransaction {
    
    func sumAmounts() -> CommonBigDecimal {
        return CommonBigDecimal(decimal_: self.map { $0.amount.decimal }.sum())
    }
    
}

extension Array where Element: OrderedDisplayable {
    
    func sortedByDisplayIndex() -> [Element] {
        return self.sorted { $0.displayIndex <= $1.displayIndex }
    }


    func reorder(from sourceIndices: IndexSet, to destinationIndex: Int) -> [Element] {
        var elements = self
        
        elements.move(fromOffsets: sourceIndices, toOffset: destinationIndex)
        
        for (index, element) in elements.enumerated() {
            element.displayIndex = Int32(index)
        }
        
        return elements.sortedByDisplayIndex()
    }
    
}


extension BankInfo : Identifiable {

    public var id: UUID { UUID() }

}


extension Remittee : Identifiable {

    public var id: String { name.localizedLowercase + "_" + (iban ?? "") }

}


extension TanProcedure : Identifiable {

    public var id: String { self.bankInternalProcedureCode }

}
