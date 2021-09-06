package com.github.vatbub.finance.manager.model

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class Account(
    val name: StringProperty,
    val transactions: ObservableList<BankTransaction>,
) : ObservableWithObservableProperties, ObservableWithObservableListProperties {
    constructor(
        name: String,
        transactions: List<BankTransaction>,
    ) : this(SimpleStringProperty(name), FXCollections.observableArrayList(transactions))

    val balance = transactions.sumOf { it.amount.value.amount }

    override val observableProperties = listOf(name)
    override val observableLists = listOf(transactions)

    fun import(bankTransactions: List<BankTransaction>) {
        transactions.addAll(
            bankTransactions
                .filterNot { bankTransaction -> transactions.contains(bankTransaction) }
        )
    }
}
