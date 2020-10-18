import SwiftUI


struct VerticalLabelledValue: View {
    
    private let label: LocalizedStringKey
    
    private let value: LocalizedStringKey
    
    
    init(_ label: LocalizedStringKey, _ value: LocalizedStringKey) {
        self.label = label
        self.value = value
    }
    
    init(_ label: LocalizedStringKey, _ value: String) {
        self.init(label, LocalizedStringKey(value))
    }
    

    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Text(label)
                
                Spacer()
            }
            
            Spacer()
            
            Text(value)
                .detailForegroundColor()
        }
    }

}


struct VerticalLabelledValue_Previews: PreviewProvider {

    static var previews: some View {
        VerticalLabelledValue("Label", "Value")
    }

}
