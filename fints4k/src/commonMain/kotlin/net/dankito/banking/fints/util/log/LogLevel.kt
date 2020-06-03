package net.dankito.banking.fints.util.log


enum class LogLevel(val priority: Int) {

    None(0),

    Fatal(1),

    Error(2),

    Warn(3),

    Info(4),

    Debug(5),

    Trace(6)

}