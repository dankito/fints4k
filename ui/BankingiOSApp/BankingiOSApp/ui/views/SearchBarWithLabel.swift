import SwiftUI


struct SearchBarWithLabel<Label: View>: View {
    
    @Binding private var searchText: String
    
    private var placeholder: String
    
    private var focusOnStart = false
    
    private var returnKeyType: UIReturnKeyType = .search
    
    private var actionOnReturnKeyPress: (() -> Bool)? = nil
    
    private var textChanged: ((String) -> Void)? = nil
    
    private let label: () -> Label
    
    
    init(_ searchText: Binding<String>, placeholder: String = "", focusOnStart: Bool = false,
         returnKeyType: UIReturnKeyType = .search, actionOnReturnKeyPress: (() -> Bool)? = nil,
         textChanged: ((String) -> Void)? = nil, @ViewBuilder _ label: @escaping () -> Label) {
        
        _searchText = searchText
        self.placeholder = placeholder
        
        self.focusOnStart = focusOnStart
        
        self.returnKeyType = returnKeyType
        
        self.actionOnReturnKeyPress = actionOnReturnKeyPress
        self.textChanged = textChanged
        
        self.label = label
    }
    

    var body: some View {
        VStack {
            UIKitSearchBar(text: $searchText, placeholder: placeholder, focusOnStart: focusOnStart, returnKeyType: returnKeyType, actionOnReturnKeyPress: actionOnReturnKeyPress, textChanged: textChanged)
            
            label()
                .padding(.horizontal)
                .padding(.bottom, 8)
        }
        .removeListInsets()
    }

}


struct SearchBarWithLabel_Previews: PreviewProvider {

    static var previews: some View {
        SearchBarWithLabel(.constant(""), {
            Text("Label")
        })
    }

}
