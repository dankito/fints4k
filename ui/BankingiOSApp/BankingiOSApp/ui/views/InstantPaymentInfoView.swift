import SwiftUI


struct InstantPaymentInfoView: View {
    
    @State private var showInstantPaymentInfoPopover = false
    

    var body: some View {
        HStack {
            Text("may with costs")
                .font(.caption)
                .styleAsDetail()
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 0)
            
            UIKitButton(.infoLight) { self.showInstantPaymentInfoPopover = true }
                .popover(isPresented: $showInstantPaymentInfoPopover, arrowEdge: .leading ) {
                    if UIDevice.isRunningOniPad {
                        ScrollView {
                            Text("Instant payment information")
                            .padding()
                            .detailForegroundColor()
                        }
                    }
                    else {
                        VStack {
                            Text("Instant payment information")
                            .padding()
                            .detailForegroundColor()

                            HStack {
                                Spacer()

                                Button("OK") { self.showInstantPaymentInfoPopover = false }

                                Spacer()
                            }
                        }
                    }
                }
        }
    }

}


struct InstantPaymentInfoView_Previews: PreviewProvider {

    static var previews: some View {
        Form {
            Section {
                Toggle(isOn: .constant(true)) {
                    InstantPaymentInfoView()
                }
            }
        }
        .environment(\.locale, .init(identifier: "de"))
    }

}
