import SwiftUI
import UIKit


struct UIKitActivityIndicator: UIViewRepresentable {
    
    private let style: UIActivityIndicatorView.Style

    @Binding private var isAnimating: Bool
    
    
    init(_ isAnimating: Binding<Bool> = .constant(true), _ style: UIActivityIndicatorView.Style = .medium) {
        _isAnimating = isAnimating
        
        self.style = style
    }
    

    func makeUIView(context: UIViewRepresentableContext<UIKitActivityIndicator>) -> UIActivityIndicatorView {
        let indicatorView = UIActivityIndicatorView(style: style)
        
        indicatorView.hidesWhenStopped = true
        
        return indicatorView
    }

    func updateUIView(_ uiView: UIActivityIndicatorView, context: UIViewRepresentableContext<UIKitActivityIndicator>) {
        isAnimating ? uiView.startAnimating() : uiView.stopAnimating()
    }
    
}


struct UIKitActivityIndicator_Previews: PreviewProvider {

    static var previews: some View {
        UIKitActivityIndicator()
    }

}
