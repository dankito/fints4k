import SwiftUI
import BankingUiSwift


struct RemitteeListItem: View {
    
    let remittee: Remittee
    
    
    var body: some View {
        VStack {
            
            HStack {
                Text(remittee.name)
                    .font(.headline)
                
                Spacer()
            }
            
            HStack {
                Text(remittee.bankName ?? "")
                
                Spacer()
            }
            .padding(.vertical, 6)
            
            HStack {
                Text(remittee.iban ?? "")
                
                Text(remittee.bic ?? "")
                
                Spacer()
            }
            .padding(.bottom, 6.0)
            
        }
    }
}

struct RemitteeListItem_Previews: PreviewProvider {
    
    static var previews: some View {
        RemitteeListItem(remittee: Remittee(name: "Marieke Musterfrau", iban: "DE12876543211234567890", bic: "ABZODEBBXX", bankName: "Abzockbank Berlin"))
    }
    
}
