package net.dankito.utils.multiplatform


enum class Month(val month: Int) {

    January(1),

    February(2),

    March(3),

    April(4),

    May(5),

    June(6),

    July(7),

    August(8),

    September(9),

    October(10),

    November(11),

    December(12);


    companion object {

        fun fromInt(monthInt: Int): Month {
            return Month.values().first { it.month == monthInt }
        }

    }

}