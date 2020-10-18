import SwiftUI


class ActionSheet : UIAlertBase {
    
    convenience init(_ title: String? = nil, _ actions: UIAlertAction...) {
        self.init(title, actions)
    }
    
    init(_ title: String? = nil, _ actions: [UIAlertAction] = []) {
        super.init(title: title, message: nil, .actionSheet, actions)
    }
    
    
    func show(_ sourceView: UIView, _ sourceRectX: CGFloat, _ sourceRectY: CGFloat) {
        if let rootViewController = SceneDelegate.rootViewController {
            let alert = createAlertController()
            
            if let popoverController = alert.popoverPresentationController {
                popoverController.sourceView = sourceView
                popoverController.sourceRect = CGRect(x: sourceRectX, y: sourceRectY, width: 0, height: 0)
            }
            
            rootViewController.present(alert, animated: true, completion: nil)
        }
    }
    
}
