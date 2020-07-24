import SwiftUI


struct UIKitSearchBar : UIViewRepresentable {
    
    @Binding var text : String
    
    
    func makeUIView(context: UIViewRepresentableContext<UIKitSearchBar>) -> UISearchBar {
        let searchBar = UISearchBar(frame: .zero)
        
        searchBar.delegate = context.coordinator
        
        return searchBar
    }
    
    func updateUIView(_ uiView: UISearchBar, context: UIViewRepresentableContext<UIKitSearchBar>) {
        uiView.text = text
    }
    
    
    class Cordinator : NSObject, UISearchBarDelegate {
        
        @Binding var text : String
        
        init(text : Binding<String>) {
            _text = text
        }
        
        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            text = searchText
        }
    }
    
    func makeCoordinator() -> Cordinator {
        return Cordinator(text: $text)
    }
    
}
