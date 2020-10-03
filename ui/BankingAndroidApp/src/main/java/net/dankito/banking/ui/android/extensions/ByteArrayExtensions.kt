package net.dankito.banking.ui.android.extensions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun ByteArray.toDrawable(resources: Resources): Drawable {
    return BitmapDrawable(resources, this.toBitmap())
}