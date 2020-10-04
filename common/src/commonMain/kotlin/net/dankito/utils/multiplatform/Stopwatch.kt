package net.dankito.utils.multiplatform

import net.dankito.utils.multiplatform.log.Logger
import net.dankito.utils.multiplatform.log.LoggerFactory


open class Stopwatch(createStarted: Boolean = true) {

    companion object {
        private val log = LoggerFactory.getLogger(Stopwatch::class)

        fun measureDuration(task: () -> Unit): Long {
            return measureDuration(Stopwatch(), task)
        }

        private fun measureDuration(stopwatch: Stopwatch, task: () -> Unit): Long {
            task()

            return stopwatch.stop()
        }

        fun <T> logDuration(loggedAction: String, task: () -> T): T {
            return logDuration(loggedAction, log, task)
        }

        fun <T> logDuration(loggedAction: String, logger: Logger, task: () -> T): T {
            val stopwatch = Stopwatch()

            val result = task()

            stopwatch.stopAndLog(loggedAction, logger)

            return result
        }


        suspend fun measureDurationSuspendable(task: suspend () -> Unit): Long {
            val stopwatch = Stopwatch()

            task()

            return stopwatch.stop()
        }

        suspend fun <T> logDurationSuspendable(loggedAction: String, task: suspend () -> T): T {
            return logDurationSuspendable(loggedAction, log, task)
        }

        suspend fun <T> logDurationSuspendable(loggedAction: String, logger: Logger, task: suspend () -> T): T {
            val stopwatch = Stopwatch()

            val result = task()

            stopwatch.stopAndLog(loggedAction, logger)

            return result
        }
    }


    constructor() : this(true)


    var isRunning = false
        protected set

    var startedAt = 0L
        protected set

    var elapsedNanos = 0L
        protected set


    init {
        if (createStarted) {
            start()
        }
    }


    open fun getCurrentTimeNanoSeconds(): Long {
        return Date.nanoSecondsSinceEpoch
    }

    open fun start() {
        if (isRunning == false) {
            startedAt = getCurrentTimeNanoSeconds()

            isRunning = true
        }
    }

    open fun stop(): Long {
        if (isRunning) {
            val stoppedAt = getCurrentTimeNanoSeconds()

            isRunning = false
            elapsedNanos = stoppedAt - startedAt
        }

        return elapsedNanos
    }

    open fun stopAndPrint(): String {
        stop()

        return formatElapsedTime()
    }

    open fun stopAndLog(loggedAction: String): Long {
        return stopAndLog(loggedAction, log)
    }

    open fun stopAndLog(loggedAction: String, logger: Logger): Long {
        stop()

        logElapsedTime(loggedAction, logger)

        return elapsedNanos
    }


//    open fun elapsed(desiredUnit: TimeUnit): Long {
//        return desiredUnit.convert(elapsedNanos, TimeUnit.NANOSECONDS)
//    }


    open fun formatElapsedTime(): String {
        return formatElapsedTime(elapsedNanos)
    }

    protected open fun formatElapsedTime(elapsedNanos: Long): String {
        val durationMicroseconds = elapsedNanos / 1000

        val durationMillis = durationMicroseconds / 1000
        val millis = durationMillis % 1000

        val durationSeconds = durationMillis / 1000
        val seconds = durationSeconds % 60

        val minutes = durationSeconds / 60

        return if (minutes > 0) {
            StringHelper.format("%02d:%02d.%03d min", minutes, seconds, millis)
        }
        else if (seconds > 0) {
            StringHelper.format("%02d.%03d s", seconds, millis)
        }
        else {
            StringHelper.format("%02d.%03d ms", millis, (durationMicroseconds % 1000))
        }
    }

    open fun logElapsedTime(loggedAction: String, logger: Logger) {
        val formattedElapsedTime = formatElapsedTime()

        logger.info("$loggedAction took $formattedElapsedTime")
    }


    override fun toString(): String {
        if (isRunning) {
            val elapsedNanos = getCurrentTimeNanoSeconds() - startedAt
            return "Running, ${formatElapsedTime(elapsedNanos)} elapsed"
        }

        return "Stopped, ${formatElapsedTime()} elapsed"
    }

}