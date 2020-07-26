import SwiftUI
import BankingUiSwift


struct BankInfoListItem: View {
    
    let bank: BankInfo
    
    
    var body: some View {
        VStack {
            HStack {
                Text(bank.name)
                    .font(.headline)

                Spacer()
            }
            
            HStack {
                Text(bank.bankCode)
                
                Text(bank.postalCode)
                    .padding(.leading, 4)
                
                Text(bank.city)
                
                Spacer()
            }
            .styleAsDetail()
            .padding(.top, 6.0)
        }
    }
}


struct BankInfoListItem_Previews: PreviewProvider {
    
    static var previews: some View {
        BankInfoListItem(bank: BankInfo(name: "Abzockbank Berlin", bankCode: "12345678", bic: "ABZODEBBXXX", postalCode: "12345", city: "Berlin", checksumMethod: "", pinTanAddress: nil, pinTanVersion: "FinTS 3.0", oldBankCode: nil))
    }
    
}
