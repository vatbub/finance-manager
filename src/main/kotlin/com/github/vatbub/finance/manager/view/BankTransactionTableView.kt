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
package com.github.vatbub.finance.manager.view

import com.github.vatbub.finance.manager.model.*
import com.github.vatbub.finance.manager.util.SortKey
import com.github.vatbub.finance.manager.util.sortKey
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import java.time.LocalDate
import kotlin.reflect.full.declaredMemberProperties

class BankTransactionTableView(items: ObservableList<BankTransaction>? = null) : TableView<BankTransaction>(items) {
    object TableColumns {
        @SortKey(0)
        val SelectedColumn = TableColumn<BankTransaction, Boolean>("Selected")
            .also { it.isEditable = true }

        @SortKey(1)
        val BookingDate = TableColumn<BankTransaction, LocalDate?>("Booking date")

        @SortKey(2)
        val ValutaDate = TableColumn<BankTransaction, LocalDate?>("Valuta")

        @SortKey(3)
        val SenderOrReceiver = TableColumn<BankTransaction, String?>("Sender / Receiver")

        @SortKey(4)
        val Iban = TableColumn<BankTransaction, IBAN?>("IBAN")

        @SortKey(5)
        val Bic = TableColumn<BankTransaction, BIC?>("BIC")

        @SortKey(6)
        val BookingText = TableColumn<BankTransaction, String?>("Booking text")

        @SortKey(7)
        val UsageText = TableColumn<BankTransaction, String?>("Usage text")

        @SortKey(8)
        val Category = TableColumn<BankTransaction, TransactionCategory?>("Category")

        @SortKey(9)
        val Tags = TableColumn<BankTransaction, String>("Tags")

        @SortKey(10)
        val Amount = TableColumn<BankTransaction, CurrencyAmount>("Amount")
    }

    @Suppress("UNCHECKED_CAST")
    private val tableColumns by lazy {
        TableColumns::class.declaredMemberProperties
            .sortedBy { it.sortKey }
            .map { it.get(TableColumns) as TableColumn<BankTransaction, *> }
    }

    init {
        addColumns()
        initCellValueFactories()
        initCellFactories()
    }

    private fun addColumns() {
        columns.addAll(tableColumns)
    }

    private fun initCellValueFactories() {
        TableColumns.SelectedColumn.cellValueFactory = BankTransaction::selected.cellValueFactory()
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

    private fun initCellFactories() {
        TableColumns.SelectedColumn.cellFactory = CheckBoxTableCell.forTableColumn(TableColumns.SelectedColumn)
    }
}
