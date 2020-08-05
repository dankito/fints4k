package net.dankito.banking.persistence.model

import com.fasterxml.jackson.annotation.*
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanProcedure
import java.util.*


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
// had to define all properties as 'var' 'cause MapStruct cannot handle vals (and cannot use Pozo's mapstruct-kotlin as SerializableCustomerBuilder would fail with @Context)
open class CustomerEntity(
    var bankCode: String,
    var customerId: String,
    var password: String,
    var finTsServerAddress: String,
    var bankName: String,
    var bic: String,
    var customerName: String,
    var userId: String = customerId,
    var iconUrl: String? = null,
    var accounts: List<BankAccountEntity> = listOf(),
    var supportedTanProcedures: List<TanProcedure> = listOf(),
    var selectedTanProcedure: TanProcedure? = null,
    var tanMedia: List<TanMedium> = listOf(),
    var id: String = UUID.randomUUID().toString()
) {

    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers

}