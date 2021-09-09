package com.github.vatbub.finance.manager.view

import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.util.Callback
import kotlin.reflect.KProperty1

fun <S, T> KProperty1<S, ObservableValue<T>>.cellValueFactory() =
    Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> { param ->
        this.get(param.value)
    }
