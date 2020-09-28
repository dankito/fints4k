import SwiftUI


/// Partially copied from https://stackoverflow.com/a/59667107
class SelectableUILabel: UITextField, UITextFieldDelegate {
    
    // change the cursor to have zero size
    override func caretRect(for position: UITextPosition) -> CGRect {
        return .zero
    }
    
    override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
    
        // disable 'cut', 'delete', 'paste','_promptForReplace:'
        // if it is not editable
        switch action {
        case #selector(cut(_:)),
             #selector(delete(_:)),
             #selector(paste(_:)):
            return false
        default:
            return super.canPerformAction(action, withSender: sender)
        }
    }
    
    
    public func showCopyContextMenuOnLongPress() {
        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(longPressed))
        self.addGestureRecognizer(longPressRecognizer)
    }
    
    @objc func longPressed(sender: UILongPressGestureRecognizer) {
        showMenu()
    }
    
    public func showMenu() {
        becomeFirstResponder()

        let menu = UIMenuController.shared
        menu.showMenu(from: self, rect: bounds)
    }
    
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return false
    }
    
}



