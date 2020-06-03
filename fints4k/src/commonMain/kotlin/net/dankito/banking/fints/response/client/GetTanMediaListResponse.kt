package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.response.Response
import net.dankito.banking.fints.response.segments.TanMediaList


open class GetTanMediaListResponse(
    response: Response,
    val tanMediaList: TanMediaList?
)
    : FinTsClientResponse(response)