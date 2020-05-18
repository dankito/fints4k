package net.dankito.banking.ui.android.views

import android.os.Bundle
import android.view.MotionEvent
import com.github.clans.fab.FloatingActionMenu
import net.dankito.utils.android.extensions.isTouchInsideView


open class FloatingActionMenuButton(protected val floatingActionMenu: FloatingActionMenu) {

    companion object {
        private const val IS_OPENED_EXTRA_NAME = "IS_OPENED"
    }


    private var isClosingMenu = false


    init {
        setup()
    }


    protected open fun setup() {
        floatingActionMenu.setClosedOnTouchOutside(true)
        floatingActionMenu.setOnMenuToggleListener { isClosingMenu = false }
    }


    protected open fun executeAndCloseMenu(action: () -> Unit) {
        action() // first execute action and then close menu as when action sets menu items visibility closeMenu() would otherwise overwrite this value
        closeMenu()
    }

    protected open fun closeMenu() {
        isClosingMenu = true // as closing is animated it takes till animation end till floatingActionMenu.isOpened is set to true
        floatingActionMenu.close(true)
    }


    open fun handlesBackButtonPress(): Boolean {
        if(floatingActionMenu.isOpened) {
            closeMenu()
            return true
        }

        return false
    }


    open fun handlesTouch(event: MotionEvent): Boolean {
        if(floatingActionMenu.isOpened) { // if menu is opened and user clicked somewhere else in the view, close menu
            if(floatingActionMenu.isTouchInsideView(event) == false) {
                closeMenu()

                return true
            }
        }

        return false
    }


    open fun saveInstanceState(outState: Bundle?) {
        outState?.let {
            outState.putBoolean(IS_OPENED_EXTRA_NAME, floatingActionMenu.isOpened && isClosingMenu == false)
        }
    }

    open fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if(savedInstanceState.getBoolean(IS_OPENED_EXTRA_NAME, false)) {
                floatingActionMenu.open(false)
            }
        }
    }

}