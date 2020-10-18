import SwiftUI


struct TextWithScrollView: View {
    
    private let value: String
    
    @State private var textFitsIntoAvailableSpace = true

    
    init(_ value: String) {
        self.value = value
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
        return SelectableText(value, UIColor.secondaryLabel, .right)

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
                                        let textsFrame = availableSpace.frame(in: .global)
                                        self.textFitsIntoAvailableSpace = self.textFitsIntoAvailableSpace == false ? false : textsFrame.maxX <= UIScreen.main.bounds.width
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
