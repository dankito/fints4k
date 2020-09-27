import SwiftUI


struct LabelledValue: View {
    
    private let label: LocalizedStringKey
    
    private let value: LocalizedStringKey
    
    @State private var textFitsIntoAvailableSpace = true
    
    
    init(_ label: LocalizedStringKey, _ value: LocalizedStringKey) {
        self.label = label
        self.value = value
    }
    
    init(_ label: LocalizedStringKey, _ value: String) {
        self.init(label, LocalizedStringKey(value))
    }
    

    var body: some View {
        HStack {
            Text(label)
            
            Spacer()
            
            TextWithScrollView(value)
        }
    }

}


struct LabelledValue_Previews: PreviewProvider {

    static var previews: some View {
        LabelledValue("Label", "Value")
    }

}


extension LabelledValue {
    
    init(_ label: LocalizedStringKey, _ value: String?) {
        self.init(label, value ?? "")
    }
    
}
