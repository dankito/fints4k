import SwiftUI


struct SelectableText: UIViewRepresentable {
    
    private let text: String
    
    private let textColor: UIColor?
    
    private let textAlignment: NSTextAlignment
    
    private var selectable: Bool
    
    
    init(_ text: String, _ textColor: UIColor? = nil, _ textAlignment: NSTextAlignment = .natural, selectable: Bool = true) {
        self.text = text
        self.textColor = textColor
        self.textAlignment = textAlignment
        self.selectable = selectable
    }
    
    func makeUIView(context: Context) -> SelectableUILabel {
        let label = SelectableUILabel()
        
        label.text = self.text
        label.textAlignment = textAlignment
        
        if let textColor = textColor {
            label.textColor = textColor
        }
        
        
        // set the input view so the keyboard does not show up
        label.inputView = UIView()
        label.autocorrectionType = .no
        
        label.showCopyContextMenuOnLongPress()
        
        return label
    }
    
    func updateUIView(_ uiView: SelectableUILabel, context: Context) {
        uiView.text = self.text
        uiView.isEnabled = self.selectable
    }
    
}
