package net.dankito.fints.model


open class ProductData(
    val name: String,
    val version: String
) {

    override fun toString(): String {
        return "$name $version"
    }

}