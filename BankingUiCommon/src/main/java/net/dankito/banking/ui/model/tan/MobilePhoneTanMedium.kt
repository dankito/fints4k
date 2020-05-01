package net.dankito.banking.ui.model.tan


open class MobilePhoneTanMedium(
    displayName: String,
    status: TanMediumStatus,
    val phoneNumber: String?

) : TanMedium(displayName, status) {

    override fun toString(): String {
        return "$displayName $status"
    }

}