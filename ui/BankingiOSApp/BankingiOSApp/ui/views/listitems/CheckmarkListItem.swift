import SwiftUI


struct CheckmarkListItem: View {
    
    let title: LocalizedStringKey
    
    @Binding var isChecked: Bool
    
    
    init(_ title: LocalizedStringKey, _ isChecked: Binding<Bool>) {
        self.title = title
        
        _isChecked = isChecked
    }
    
    init(_ title: LocalizedStringKey, _ isChecked: Bool) {
        self.init(title, .constant(isChecked))
    }
    

    var body: some View {
        HStack {
            HStack {
                if isChecked {
                    Image(systemName: "checkmark")
                    
                    Spacer()
                }
                
            }
            .frame(width: 25)
            
            if isChecked {
                Text(title)
            }
            else {
                Text(title)
                .detailForegroundColor()
            }
            
            Spacer()
        }
    }

}


struct CheckmarkListItem_Previews: PreviewProvider {

    static var previews: some View {
        Group {
            CheckmarkListItem("Title", true)
            .previewLayout(PreviewLayout.sizeThatFits)
            
            CheckmarkListItem("Title", false)
            .previewLayout(PreviewLayout.sizeThatFits)
        }
    }

}
