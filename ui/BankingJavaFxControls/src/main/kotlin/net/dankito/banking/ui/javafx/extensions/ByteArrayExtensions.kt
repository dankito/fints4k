package net.dankito.banking.ui.javafx.extensions

import javafx.scene.image.Image
import java.io.ByteArrayInputStream


fun ByteArray.toImage(): Image {
    return Image(ByteArrayInputStream(this))
}