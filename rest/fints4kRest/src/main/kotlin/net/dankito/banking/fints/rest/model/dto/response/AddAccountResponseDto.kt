package net.dankito.banking.fints.rest.model.dto.response


open class AddAccountResponseDto(
    successful: Boolean,
    errorMessage: String?,
    open val bank: BankResponseDto,
    open val accounts: List<BankAccountResponseDto>
) : ResponseDtoBase(successful, errorMessage)