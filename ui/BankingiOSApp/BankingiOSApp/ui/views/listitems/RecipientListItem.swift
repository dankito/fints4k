import SwiftUI
import BankingUiSwift


struct RecipientListItem: View {
    
    let recipient: TransactionParty
    
    
    var body: some View {
        VStack {
            
            HStack {
                Text(recipient.name)
                    .bold()
                    .lineLimit(1)
                
                Spacer()
            }
            .padding(.vertical, 6)
            
            recipient.bankName.map { bankName in
                HStack {
                    Text(bankName)
                        .font(.footnote)
                        .lineLimit(1)
                    
                    Spacer()
                }
                .padding(.bottom, 6)
            }
            
            HStack {
                Text(recipient.iban ?? "")
                
                Spacer()
                
                Text(recipient.bic ?? "")
            }
            .font(.footnote)
            .lineLimit(1)
            .padding(.bottom, 6.0)
            
        }
        .makeBackgroundTapable()
    }
}

struct RecipientListItem_Previews: PreviewProvider {
    
    static var previews: some View {
        RecipientListItem(recipient: TransactionParty(name: "Marieke Musterfrau", iban: "DE12876543211234567890", bic: "ABZODEBBXX", bankName: "Abzockbank Berlin"))
    }
    
}
