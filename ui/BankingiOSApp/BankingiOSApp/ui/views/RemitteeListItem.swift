import SwiftUI
import BankingUiSwift


struct RemitteeListItem: View {
    
    let remittee: Remittee
    
    
    var body: some View {
        VStack {
            
            HStack {
                Text(remittee.name)
                    .bold()
                    .lineLimit(1)
                
                Spacer()
            }
            .padding(.vertical, 6)
            
            remittee.bankName.map { bankName in
                HStack {
                    Text(bankName)
                        .font(.footnote)
                        .lineLimit(1)
                    
                    Spacer()
                }
                .padding(.bottom, 6)
            }
            
            HStack {
                Text(remittee.iban ?? "")
                
                Spacer()
                
                Text(remittee.bic ?? "")
            }
            .font(.footnote)
            .lineLimit(1)
            .padding(.bottom, 6.0)
            
        }
    }
}

struct RemitteeListItem_Previews: PreviewProvider {
    
    static var previews: some View {
        RemitteeListItem(remittee: Remittee(name: "Marieke Musterfrau", iban: "DE12876543211234567890", bic: "ABZODEBBXX", bankName: "Abzockbank Berlin"))
    }
    
}
