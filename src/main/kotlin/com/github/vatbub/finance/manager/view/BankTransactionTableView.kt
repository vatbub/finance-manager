package com.github.vatbub.finance.manager.view

import com.github.vatbub.finance.manager.model.*
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import java.time.LocalDate
import kotlin.reflect.full.declaredMemberProperties

class BankTransactionTableView(items: ObservableList<BankTransaction>? = null) : TableView<BankTransaction>(items) {
    object TableColumns {
        val BookingDate = TableColumn<BankTransaction, LocalDate?>("Booking date")
        val ValutaDate = TableColumn<BankTransaction, LocalDate?>("Valuta")
        val SenderOrReceiver = TableColumn<BankTransaction, String?>("Sender / Receiver")
        val Iban = TableColumn<BankTransaction, IBAN?>("IBAN")
        val Bic = TableColumn<BankTransaction, BIC?>("BIC")
        val BookingText = TableColumn<BankTransaction, String?>("Booking text")
        val UsageText = TableColumn<BankTransaction, String?>("Usage text")
        val Category = TableColumn<BankTransaction, TransactionCategory?>("Category")
        val Tags = TableColumn<BankTransaction, String>("Tags")
        val Amount = TableColumn<BankTransaction, CurrencyAmount>("Amount")
    }

    @Suppress("UNCHECKED_CAST")
    private val tableColumns by lazy {
        TableColumns::class.declaredMemberProperties.map { it.get(TableColumns) as TableColumn<BankTransaction, *> }
    }

    init {
        addColumns()
        initCellValueFactories()
    }

    private fun addColumns() {
        columns.addAll(tableColumns)
    }

    private fun initCellValueFactories() {
        TableColumns.BookingDate.cellValueFactory = PropertyValueFactory("bookingDate")
        TableColumns.ValutaDate.cellValueFactory = PropertyValueFactory("valutaDate")
        TableColumns.SenderOrReceiver.cellValueFactory = PropertyValueFactory("senderOrReceiver")
        TableColumns.Iban.cellValueFactory = PropertyValueFactory("iban")
        TableColumns.Bic.cellValueFactory = PropertyValueFactory("bic")
        TableColumns.BookingText.cellValueFactory = PropertyValueFactory("bookingText")
        TableColumns.UsageText.cellValueFactory = PropertyValueFactory("usageText")
        TableColumns.Category.cellValueFactory = PropertyValueFactory("category")
        TableColumns.Tags.cellValueFactory = PropertyValueFactory("tags")
        TableColumns.Amount.cellValueFactory = PropertyValueFactory("amount")
    }
}
