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
