package net.dankito.fints.extensions


fun String.allIndicesOf(toFind: String): List<Int> {
    val indices = mutableListOf<Int>()
    var index = -1

    do {
        index = this.indexOf(toFind, index + 1)

        if (index > -1) {
            indices.add(index)
        }
    } while (index > -1)

    return indices
}