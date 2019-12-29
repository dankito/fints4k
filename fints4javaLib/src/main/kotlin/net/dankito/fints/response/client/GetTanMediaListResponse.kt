package net.dankito.fints.response.client

import net.dankito.fints.response.Response
import net.dankito.fints.response.segments.TanMediaList


open class GetTanMediaListResponse(
    response: Response,
    val tanMediaList: TanMediaList?
)
    : FinTsClientResponse(response)