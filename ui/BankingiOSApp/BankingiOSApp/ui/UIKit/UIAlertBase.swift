import SwiftUI


class UIAlertBase {
    
    private var title: String? = nil
    
    private var message: String? = nil
    
    private var preferredStyle: UIAlertController.Style = .alert
    
    private var actions: [UIAlertAction] = []
    
    
    init(title: String? = nil, message: String? = nil, _ preferredStyle: UIAlertController.Style = .alert, _ actions: [UIAlertAction] = []) {
        self.title = title
        self.message = message
        self.preferredStyle = preferredStyle
        self.actions = actions
    }
    
    
    func createAlertController() -> UIAlertController {
        let alert = UIAlertController(title: title?.localize(), message: message?.localize(), preferredStyle: preferredStyle)

        for action in actions {
            alert.addAction(action)
        }
        
        return alert
    }
    
}
