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
package com.github.vatbub.finance.manager.util

import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

fun <T> ObservableList<T>.bind(other: ObservableList<T>) {
    other.addListener(ListChangeListener { change ->
        while (change.next()) {
            try {
                handleChange(change)
            } catch (e: IllegalStateException) {
                Platform.runLater { handleChange(change) }
            }
        }
    })
}

private fun <T> ObservableList<T>.handleChange(change: ListChangeListener.Change<out T>) {
    if (change.wasAdded()) this.addAll(change.from, change.addedSubList)
    if (change.wasRemoved()) this.removeAll(change.removed)
    if (change.wasPermutated()) {
        this.indices.forEach { oldIndex ->
            this[change.getPermutation(oldIndex)] = this[oldIndex]
        }
    }
}
