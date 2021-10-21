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
package com.github.vatbub.finance.manager.model

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

interface ObservableWithObservableProperties {
    val observableProperties: List<ObservableValue<*>>

    fun addListenerToAllProperties(listener: ChangeListener<Any>) =
        observableProperties.forEach { it.addListener(listener) }

    fun removeListenerFromAllProperties(listener: ChangeListener<Any>) =
        observableProperties.forEach { it.removeListener(listener) }
}

interface ObservableWithObservableListProperties {
    val observableLists: List<ObservableList<*>>

    fun addListenerToAllLists(listener: ListChangeListener<Any>) =
        observableLists.forEach { it.addListener(listener) }

    fun removeListenerFromAllLists(listener: ListChangeListener<Any>) =
        observableLists.forEach { it.removeListener(listener) }
}
