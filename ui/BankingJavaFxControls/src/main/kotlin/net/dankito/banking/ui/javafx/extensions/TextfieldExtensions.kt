package net.dankito.banking.ui.javafx.extensions

import com.sun.javafx.scene.traversal.Direction
import javafx.scene.control.TextField


fun TextField.focusNextControl() {
    this.impl_traverse(Direction.NEXT)
}