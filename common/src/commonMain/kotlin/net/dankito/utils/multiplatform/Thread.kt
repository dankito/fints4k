package net.dankito.utils.multiplatform


expect class Thread() {

    companion object {

        val current: Thread

        fun printCurrentThreadStackTrace()

    }


    val threadName: String

    fun printStackTrace()

}