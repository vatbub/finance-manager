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
