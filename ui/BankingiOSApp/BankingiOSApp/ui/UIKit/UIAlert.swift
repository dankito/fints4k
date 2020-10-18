import SwiftUI


class UIAlert : UIAlertBase {
    
    convenience init(_ title: String? = nil, _ message: String? = nil, _ actions: UIAlertAction...) {
        self.init(title, message, actions)
    }
    
    init(_ title: String? = nil, _ message: String? = nil, _ actions: [UIAlertAction] = []) {
        super.init(title: title, message: message, .alert, actions)
    }
    
    
    func show() {
        if let rootViewController = SceneDelegate.rootViewController {
            let alert = createAlertController()
            
            rootViewController.present(alert, animated: true)
        }
    }
    
}
