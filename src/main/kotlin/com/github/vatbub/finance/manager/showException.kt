/*-
 * #%L
 * finance-manager
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.finance.manager

import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.StringWriter
import java.util.logging.Level
import java.util.logging.LogRecord

fun showException(record: LogRecord) {
    val alertType = when (record.level) {
        Level.CONFIG -> Alert.AlertType.INFORMATION
        Level.INFO -> Alert.AlertType.INFORMATION
        Level.WARNING -> Alert.AlertType.WARNING
        Level.SEVERE -> Alert.AlertType.ERROR
        else -> Alert.AlertType.INFORMATION
    }

    val alert = Alert(alertType)
    alert.title = when (record.level) {
        Level.CONFIG -> "Finance manager: Configuration message"
        Level.INFO -> "Finance manager: Information"
        Level.WARNING -> "Finance manager: Warning"
        Level.SEVERE -> "Finance manager: Error"
        else -> "Finance manager: Information"
    }
    alert.headerText = when (record.level) {
        Level.CONFIG -> "Configuration message."
        Level.INFO -> "Information"
        Level.WARNING -> "A warning occurred."
        Level.SEVERE -> "An error occurred."
        else -> "Information"
    }

    val throwable = record.thrown
    val rootCause = if (throwable == null) null else ExceptionUtils.getRootCause(throwable)!!

    val contentTextBuilder = StringBuilder(record.message)
    if (rootCause != null) {
        contentTextBuilder.append("${rootCause.javaClass.name}: ${rootCause.message}")
        val stringWriter = StringWriter()
        stringWriter.write(rootCause.stackTraceToString())

        val label = Label("The stacktrace was:")
        val textArea = TextArea(stringWriter.toString())
        with(textArea) {
            isWrapText = false
            isEditable = false
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
        }
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expandableContent = GridPane()
        with(expandableContent) {
            maxWidth = Double.MAX_VALUE
            add(label, 0, 0)
            add(textArea, 0, 1)
        }

        alert.dialogPane.expandableContent = expandableContent
    }

    alert.contentText = contentTextBuilder.toString()

    alert.show()
}
