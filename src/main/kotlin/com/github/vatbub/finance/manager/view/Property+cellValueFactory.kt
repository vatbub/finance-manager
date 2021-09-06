package com.github.vatbub.finance.manager.view

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.util.Callback
import kotlin.reflect.KProperty1

fun <S, T> KProperty1<S, ObservableValue<T>>.observableCellValueFactory() =
    Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> { param ->
        this.get(param.value)
    }

fun <S, T> KProperty1<S, T>.cellValueFactory() =
    Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> { param ->
        SimpleObjectProperty(this.get(param.value))
    }
