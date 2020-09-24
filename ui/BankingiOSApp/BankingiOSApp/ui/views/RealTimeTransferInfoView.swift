import SwiftUI


struct RealTimeTransferInfoView: View {
    
    @State private var showRealTimeTransferInfoPopover = false
    

    var body: some View {
        HStack {
            Text("may with costs")
                .font(.caption)
                .styleAsDetail()
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 0)
            
            UIKitButton(.infoLight) { self.showRealTimeTransferInfoPopover = true }
                .frame(width: 15, height: 15)
                .popover(isPresented: $showRealTimeTransferInfoPopover, arrowEdge: .leading ) {
                    if UIDevice.isRunningOniPad {
                        ScrollView {
                            Text("Real-time transfer information")
                            .padding()
                            .detailForegroundColor()
                        }
                    }
                    else {
                        VStack {
                            Text("Real-time transfer information")
                            .padding()
                            .detailForegroundColor()

                            HStack {
                                Spacer()

                                Button("OK") { self.showRealTimeTransferInfoPopover = false }

                                Spacer()
                            }
                        }
                    }
                }
        }
    }

}


struct RealTimeTransferInfoView_Previews: PreviewProvider {

    static var previews: some View {
        Form {
            Section {
                Toggle(isOn: .constant(true)) {
                    RealTimeTransferInfoView()
                }
            }
        }
        .environment(\.locale, .init(identifier: "de"))
    }

}
