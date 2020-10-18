package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.response.BankResponse
import net.dankito.banking.fints.response.segments.TanMediaList


open class GetTanMediaListResponse(
    response: BankResponse,
    val tanMediaList: TanMediaList?
)
    : FinTsClientResponse(response)