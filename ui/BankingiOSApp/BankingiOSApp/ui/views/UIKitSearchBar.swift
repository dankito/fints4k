import SwiftUI


struct UIKitSearchBar : UIViewRepresentable {
    
    @Binding var text : String
    
    private var placeholder: String
    
    private var focusOnStart = false
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    private var textChanged: ((String) -> Void)? = nil
    
    
    init(text: Binding<String>, placeholder: String = "", focusOnStart: Bool = false, actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
        _text = text
        self.placeholder = placeholder
        
        self.focusOnStart = focusOnStart
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
        self.textChanged = textChanged
    }
    
    
    func makeUIView(context: UIViewRepresentableContext<UIKitSearchBar>) -> UISearchBar {
        let searchBar = UISearchBar(frame: .zero)
        
        searchBar.placeholder = placeholder.localize()

        searchBar.searchBarStyle = .minimal
        
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
        return Cordinator(text: $text, actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
    }
    
    
    class Cordinator : NSObject, UISearchBarDelegate, UITextFieldDelegate {
        
        @Binding var text : String
        
        private var actionOnReturnKeyPress: (() -> Bool)?
        
        private var textChanged: ((String) -> Void)?


        init(text: Binding<String>, actionOnReturnKeyPress: (() -> Bool)? = nil, textChanged: ((String) -> Void)? = nil) {
            _text = text
            
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
            return actionOnReturnKeyPress?() ?? false
        }
        
        
    }
    
}
