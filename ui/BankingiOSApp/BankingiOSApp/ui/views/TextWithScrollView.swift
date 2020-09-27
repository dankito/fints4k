import SwiftUI


struct TextWithScrollView: View {
    
    private let value: LocalizedStringKey
    
    @State private var textFitsIntoAvailableSpace = true
    
    
    init( _ value: LocalizedStringKey) {
        self.value = value
    }
    
    init(_ value: String) {
        self.init(LocalizedStringKey(value))
    }
    

    var body: some View {
        if textFitsIntoAvailableSpace {
            valueText
        }
        else {
            ScrollView(.horizontal) {
                valueText
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


struct TextWithScrollView_Previews: PreviewProvider {

    static var previews: some View {
        TextWithScrollView("Text")
    }

}
