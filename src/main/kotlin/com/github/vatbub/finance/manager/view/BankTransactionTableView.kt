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
        TableColumns.BookingDate.cellValueFactory = BankTransaction::bookingDate.cellValueFactory()
        TableColumns.ValutaDate.cellValueFactory = BankTransaction::valutaDate.cellValueFactory()
        TableColumns.SenderOrReceiver.cellValueFactory = BankTransaction::senderOrReceiver.cellValueFactory()
        TableColumns.Iban.cellValueFactory = BankTransaction::iban.cellValueFactory()
        TableColumns.Bic.cellValueFactory = BankTransaction::bic.cellValueFactory()
        TableColumns.BookingText.cellValueFactory = BankTransaction::bookingText.cellValueFactory()
        TableColumns.UsageText.cellValueFactory = BankTransaction::usageText.cellValueFactory()
        TableColumns.Category.cellValueFactory = BankTransaction::category.cellValueFactory()
        TableColumns.Tags.cellValueFactory = PropertyValueFactory("tags") // TODO add edit view
        TableColumns.Amount.cellValueFactory = BankTransaction::amount.cellValueFactory()
    }
}
