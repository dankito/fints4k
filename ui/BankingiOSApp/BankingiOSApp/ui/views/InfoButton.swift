import SwiftUI


struct InfoButton: View {
    
    @State private var showInfoPopover = false
    
    private let infoText: LocalizedStringKey
    
    private let arrowEdge: Edge
    
    
    init(_ infoText: LocalizedStringKey, _ arrowEdge: Edge = .leading) {
        self.infoText = infoText
        self.arrowEdge = arrowEdge
    }
    

    var body: some View {
        UIKitButton(.infoLight) { self.showInfoPopover = true }
            .frame(width: 15, height: 15)
            .popover(isPresented: $showInfoPopover, arrowEdge: arrowEdge ) {
                if UIDevice.isRunningOniPad {
                    ScrollView {
                        Text(infoText)
                        .padding()
                        .detailForegroundColor()
                    }
                }
                else {
                    VStack {
                        Text(infoText)
                        .padding()
                        .detailForegroundColor()

                        HStack {
                            Spacer()

                            Button(action: { self.showInfoPopover = false }) {
                                Text("OK")
                                    .frame(minWidth: 120, minHeight: 35)
                            }

                            Spacer()
                        }
                    }
                }
            }
    }

}


struct InfoButton_Previews: PreviewProvider {

    static var previews: some View {
        InfoButton("Info")
    }

}
