package net.dankito.banking.ui.javafx.extensions

import javafx.scene.image.ImageView
import net.dankito.banking.ui.model.IBankData


fun IBankData<*, *>.createBankIconImageView(iconSize: Double): ImageView {
    // TODO: set default icon if iconData is null
    val image = iconData?.toImage()
    val iconImageView = ImageView(image)

    iconImageView.fitHeight = iconSize
    iconImageView.fitWidth = iconSize
    iconImageView.isPreserveRatio = true

    return iconImageView
}