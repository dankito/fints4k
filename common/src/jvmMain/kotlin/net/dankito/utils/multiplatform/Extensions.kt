package net.dankito.utils.multiplatform


fun java.util.Date.toDate(): Date {
    return Date(this.time)
}