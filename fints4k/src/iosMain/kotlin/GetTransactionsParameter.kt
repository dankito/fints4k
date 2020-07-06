import net.dankito.banking.fints.model.AccountTransaction
import platform.Foundation.NSDate


class GetTransactionsParameter(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: NSDate? = null,
    val toDate: NSDate? = null,
    val maxCountEntries: Int? = null,
    val abortIfTanIsRequired: Boolean = false,
    val retrievedChunkListener: ((List<AccountTransaction>) -> Unit)? = null
) {

    constructor() : this(true, null, null, null, false, null)

}