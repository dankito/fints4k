package net.dankito.banking.fints.rest.model.dto.response


open class ResponseDtoBase(
    open val successful: Boolean,
    open val errorMessage: String?
)