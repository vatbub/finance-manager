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

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

class Account(
    val name: StringProperty,
    val transactions: ObservableList<BankTransaction>,
) : ObservableWithObservableProperties, ObservableWithObservableListProperties {
    constructor(
        name: String,
        transactions: List<BankTransaction>,
    ) : this(SimpleStringProperty(name), FXCollections.observableArrayList(transactions))

    private fun calculateBalance(transactions: List<BankTransaction> = this.transactions) =
        transactions.sumOf { it.amount.value.amount }

    private val balanceProperty = SimpleDoubleProperty(calculateBalance())
    val balance: ReadOnlyDoubleProperty = balanceProperty

    override val observableProperties = listOf(name, balance)
    override val observableLists = listOf(transactions)

    init {
        transactions.addListener(ListChangeListener {
            balanceProperty.value = calculateBalance(it.list)
        })
    }

    fun import(bankTransactions: List<BankTransaction>) {
        transactions.addAll(
            bankTransactions
                .filterNot { bankTransaction -> transactions.contains(bankTransaction) }
        )
    }
}
