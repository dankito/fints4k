package net.dankito.banking.bankfinder

import net.dankito.utils.multiplatform.getString
import net.dankito.utils.multiplatform.getStringOrEmpty
import net.dankito.utils.multiplatform.toList
import platform.Foundation.*


actual class BankListDeserializer {

    actual fun loadBankList(): List<BankInfo> {
        val bundle = NSBundle.mainBundle

        bundle.pathForResource("BankList", "json")?.let { bankListJsonPath ->
            NSData.dataWithContentsOfFile(bankListJsonPath, NSDataReadingMappedIfSafe, null)?.let { bankListData ->
                NSJSONSerialization.JSONObjectWithData(bankListData, NSJSONReadingMutableContainers, null)?.let { bankListJson ->
                    (bankListJson as? NSArray)?.let {
                        return bankListJson.toList<NSDictionary>().map {
                            mapToBankInfo(it)
                        }
                    }
                }
            }
        }


        return listOf()
    }

    private fun mapToBankInfo(bankInfoDict: NSDictionary): BankInfo {
        return BankInfo(
            bankInfoDict.getStringOrEmpty("name"),
            bankInfoDict.getStringOrEmpty("bankCode"),
            bankInfoDict.getStringOrEmpty("bic"),
            bankInfoDict.getStringOrEmpty("postalCode"),
            bankInfoDict.getStringOrEmpty("city"),
            bankInfoDict.getStringOrEmpty("checksumMethod"),
            bankInfoDict.getString("pinTanAddress"),
            bankInfoDict.getString("pinTanVersion"),
            bankInfoDict.getString("oldBankCode")
        )
    }

}