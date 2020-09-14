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
            
            if textFitsIntoAvailableSpace {
                valueText
            }
            else {
                ScrollView(.horizontal) {
                    valueText
                }
            }
        }
    }
    
    private var valueText: some View {
        return Text(value)
            .detailForegroundColor()

            .background(

                // Render the limited text and measure its size
                Text(value).lineLimit(1)
                    .background(GeometryReader { availableSpace in

                        ZStack {

                            // Render the text without restrictions and measure its size
                            Text(self.value)
                                .background(GeometryReader { requiredSpace in

                                    // And compare the two
                                    Color.clear.onAppear {
                                        self.textFitsIntoAvailableSpace = self.textFitsIntoAvailableSpace == false ? false : availableSpace.size.width >= requiredSpace.size.width
                                    }
                                })
                        }
                        .frame(width: .greatestFiniteMagnitude)
                    })
                    .hidden() // Hide the background
            )
    }

}


struct LabelledValue_Previews: PreviewProvider {

    static var previews: some View {
        LabelledValue("Label", "Value")
    }

}
