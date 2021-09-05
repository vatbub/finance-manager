package com.github.vatbub.finance.manager.util

import javafx.util.StringConverter

object StringStringConverter : StringConverter<String>() {
    override fun toString(`object`: String?): String = `object` ?: ""

    override fun fromString(string: String?): String = string ?: ""
}
