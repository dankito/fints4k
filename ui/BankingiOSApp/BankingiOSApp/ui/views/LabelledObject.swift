import SwiftUI


struct LabelledObject<Content: View>: View {
    
    private let label: LocalizedStringKey
    
    private let objectView: () -> Content
    
    
    init(_ label: LocalizedStringKey, @ViewBuilder _ objectView: @escaping () -> Content) {
        self.label = label
        self.objectView = objectView
    }

    var body: some View {
        HStack {
            Text(label)
            
            Spacer()
            
            objectView()
        }
    }

}


struct LabelledObject_Previews: PreviewProvider {

    static var previews: some View {
        LabelledObject("Label") {
            Text("Value")
        }
    }

}
