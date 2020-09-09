import SwiftUI


struct UIKitSearchBar : UIViewRepresentable {
    
    @Binding var text : String
    
    private var placeholder: String
    
    private var focusOnStart = false
    
    private var returnKeyType: UIReturnKeyType = .search
    
    private var hideKeyboardOnReturnKeyPress = true
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    private var textChanged: ((String) -> Void)? = nil
    
    
    init(text: Binding<String>, placeholder: String = "", focusOnStart: Bool = false,
         returnKeyType: UIReturnKeyType = .search, hideKeyboardOnReturnKeyPress: Bool = true,
         actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
        
        _text = text
        self.placeholder = placeholder
        
        self.focusOnStart = focusOnStart
        
        self.returnKeyType = returnKeyType
        self.hideKeyboardOnReturnKeyPress = hideKeyboardOnReturnKeyPress
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
        self.textChanged = textChanged
    }
    
    
    func makeUIView(context: UIViewRepresentableContext<UIKitSearchBar>) -> UISearchBar {
        let searchBar = UISearchBar(frame: .zero)
        
        searchBar.placeholder = placeholder.localize()

        searchBar.searchBarStyle = .minimal
        searchBar.autocapitalizationType = .none
        
        searchBar.returnKeyType = returnKeyType
        searchBar.enablesReturnKeyAutomatically = false // so that return key also gets enabled if no text is entered
        
        searchBar.delegate = context.coordinator
        searchBar.searchTextField.delegate = context.coordinator
        
        if focusOnStart {
            searchBar.focus()
        }
        
        return searchBar
    }
    
    func updateUIView(_ uiView: UISearchBar, context: UIViewRepresentableContext<UIKitSearchBar>) {
        uiView.text = text
    }
    
    func makeCoordinator() -> Cordinator {
        return Cordinator($text, hideKeyboardOnReturnKeyPress, actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
    }
    
    
    class Cordinator : NSObject, UISearchBarDelegate, UITextFieldDelegate {
        
        @Binding var text : String
        
        private var hideKeyboardOnReturnKeyPress: Bool
        
        private var actionOnReturnKeyPress: (() -> Bool)?
        
        private var textChanged: ((String) -> Void)?


        init(_ text: Binding<String>, _ hideKeyboardOnReturnKeyPress: Bool, actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
            _text = text
            
            self.hideKeyboardOnReturnKeyPress = hideKeyboardOnReturnKeyPress
            
            self.actionOnReturnKeyPress = actionOnReturnKeyPress
            
            self.textChanged = textChanged
        }
        
        
        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            let newText = searchBar.text ?? ""
            let didTextChange = newText != text // e.g. if just the cursor has been placed to another position then textFieldDidChangeSelection() gets called but text didn't change

            DispatchQueue.main.async { // to not update state during view update
                self.text = newText

                if didTextChange {
                    self.textChanged?(newText)
                }
            }
        }
        
        
        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            let didHandleReturnKey = actionOnReturnKeyPress?() ?? false

            if hideKeyboardOnReturnKeyPress || (textField.returnKeyType != .search && didHandleReturnKey == false) {
                textField.clearFocus() // default behaviour
            }
            
            return didHandleReturnKey
        }
        
        
    }
    
}
