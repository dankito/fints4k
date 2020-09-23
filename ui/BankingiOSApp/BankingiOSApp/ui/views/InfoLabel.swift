import SwiftUI


struct InfoLabel: View {
    
    private let information: LocalizedStringKey
    
    
    init(_ information: String) {
        self.init(LocalizedStringKey(information))
    }
    
    init(_ information: LocalizedStringKey) {
        self.information = information
    }
    

    var body: some View {
        VStack {
            Spacer()
            .frame(height: 6)
            
            HStack {
                Text(information)
                .padding(.leading, 16)
                
                Spacer()
            }
            
            Spacer()
            .frame(height: 18)
        }
        .font(.callout)
        .removeSectionBackground()
    }

}


struct InfoLabel_Previews: PreviewProvider {

    static var previews: some View {
        InfoLabel("A nice info")
    }

}
