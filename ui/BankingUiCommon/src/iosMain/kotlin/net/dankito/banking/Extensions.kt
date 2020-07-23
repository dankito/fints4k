package net.dankito.banking

import net.dankito.banking.ui.model.tan.TanImage
import net.dankito.utils.multiplatform.toNSData
import platform.Foundation.NSData


fun TanImage.imageBytesAsNSData(): NSData {
    return imageBytes.toNSData()
}