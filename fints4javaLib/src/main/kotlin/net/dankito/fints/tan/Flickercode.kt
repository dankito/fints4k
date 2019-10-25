package net.dankito.fints.tan


open class Flickercode(
    val challenge: String,
    val challengeLength: Int,
    val hasControlByte: Boolean,
    val startCodeEncoding: FlickercodeEncoding,
    val startCodeLength: Int,
    val startCode: String,
    val luhnChecksum: Int,
    val xorChecksum: String,
    val rendered: String
)