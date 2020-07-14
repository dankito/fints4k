package net.dankito.utils.multiplatform


expect class Thread() {

    companion object {

        val current: Thread

    }


    val threadName: String

}