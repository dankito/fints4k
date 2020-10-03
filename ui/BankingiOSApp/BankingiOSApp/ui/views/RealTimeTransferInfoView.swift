import SwiftUI


struct RealTimeTransferInfoView: View {
    

    var body: some View {
        HStack {
            Text("may with costs")
                .font(.caption)
                .styleAsDetail()
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 0)
            
            InfoButton("Real-time transfer information")
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
