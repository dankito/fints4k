import SwiftUI


struct UIKitButton: UIViewRepresentable {

    private let type: UIButton.ButtonType
    
    private let text: String?
    
    private let selectorWrapper: SelectorWrapper


    init(_ type: UIButton.ButtonType = .system, text: String? = nil, _ action: @escaping () -> Void) {
        self.type = type
        
        self.text = text
        
        self.selectorWrapper = SelectorWrapper(action)
    }


    func makeUIView(context: UIViewRepresentableContext<UIKitButton>) -> UIButton {
        let button = UIButton(type: type)
        
        if let text = text {
            button.setTitle(text, for: .normal)
        }
        
        button.addTarget(selectorWrapper, action: #selector(selectorWrapper.action), for: .touchUpInside)
        
        return button
    }

    func updateUIView(_ uiView: UIButton, context: UIViewRepresentableContext<UIKitButton>) {
        uiView.addTarget(selectorWrapper, action: #selector(selectorWrapper.action), for: .touchUpInside) // why gets the target removed at some point and needs to be refreshed?
    }

}


struct UIKitButton_Previews: PreviewProvider {

    static var previews: some View {
        UIKitButton(.infoLight) { }
    }

}
