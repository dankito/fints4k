import SwiftUI


/**
    Adds a button to expand / collapse long texts.
 
    Code copied in large parts from Rob Napier (see https://stackoverflow.com/a/63102244/8837882).
 */
struct CollapsibleText: View {

    /* Indicates whether the user want to see all the text or not. */
    @State private var isExpanded: Bool = false

    /* Indicates whether the text has been truncated in its display. */
    @State private var isTruncated: Bool = false

    private var text: String

    private var lineLimit: Int
    

    init(_ text: String, lineLimit: Int = 3) {
        self.text = text
        self.lineLimit = lineLimit
    }
    

    var body: some View {
        VStack(alignment: .leading) {
            // Render the real text (which might or might not be limited)
            Text(text)
                .lineLimit(isExpanded ? nil : lineLimit)

                .background(

                    // Render the limited text and measure its size
                    Text(text).lineLimit(lineLimit)
                        .background(GeometryReader { displayedGeometry in

                            // Create a ZStack with unbounded height to allow the inner Text as much
                            // height as it likes, but no extra width.
                            ZStack {

                                // Render the text without restrictions and measure its size
                                Text(self.text)
                                    .background(GeometryReader { fullGeometry in

                                        // And compare the two
                                        Color.clear.onAppear {
                                            self.isTruncated = fullGeometry.size.height > displayedGeometry.size.height
                                        }
                                    })
                            }
                            .frame(height: .greatestFiniteMagnitude)
                        })
                        .hidden() // Hide the background
                )

            if isTruncated {
                Button(action: { self.isExpanded.toggle() }) {
                    Text(self.isExpanded ? "Show less ..." : "Show all ...")
                        .font(.caption)
                }
                .padding(.top, 2)
            }
        }
    }
    
}


struct LongText_Previews: PreviewProvider {
    static var previews: some View {
        CollapsibleText("Short text")
    }
}
