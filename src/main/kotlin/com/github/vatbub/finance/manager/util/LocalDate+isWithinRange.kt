package com.github.vatbub.finance.manager.util

import java.time.LocalDate

fun LocalDate.isWithinRange(startDate: LocalDate, endDate: LocalDate): Boolean {
    return !(this.isBefore(startDate) || this.isAfter(endDate))
}
