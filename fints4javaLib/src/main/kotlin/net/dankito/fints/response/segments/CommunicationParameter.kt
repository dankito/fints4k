package net.dankito.fints.response.segments


open class CommunicationParameter(
    val type: Kommunikationsdienst,
    val address: String
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommunicationParameter) return false

        if (type != other.type) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }


    override fun toString(): String {
        return "$type $address"
    }

}