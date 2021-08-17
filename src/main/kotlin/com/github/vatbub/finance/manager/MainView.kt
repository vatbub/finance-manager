/*-
 * #%L
 * Smart Charge
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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
package com.github.vatbub.finance.manager

import com.github.vatbub.finance.manager.database.DatabaseManager
import com.github.vatbub.finance.manager.util.bindAndMap
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import java.net.URL
import java.time.LocalDate
import java.util.*


class MainView {

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var textFieldDatabaseLocation: TextField

    @FXML
    private lateinit var choiceBoxImportSource: ChoiceBox<ImportSource>

    @FXML
    private lateinit var labelDatabaseCount: Label

    @FXML
    private lateinit var tableViewTransactionsToBeImported: TableView<BankTransaction>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedBookingDate: TableColumn<BankTransaction, LocalDate?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedValutaDate: TableColumn<BankTransaction, LocalDate?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedSenderOrReceiver: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedIban: TableColumn<BankTransaction, IBAN?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedBic: TableColumn<BankTransaction, BIC?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedBookingText: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedUsageText: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedCategory: TableColumn<BankTransaction, TransactionCategory?>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedTags: TableColumn<BankTransaction, String>

    @FXML
    private lateinit var tableColumnTransactionsToBeImportedAmount: TableColumn<BankTransaction, CurrencyAmount>

    @FXML
    private lateinit var tableViewDatabaseContents: TableView<BankTransaction>

    @FXML
    private lateinit var tableColumnDatabaseContentsBookingDate: TableColumn<BankTransaction, LocalDate?>

    @FXML
    private lateinit var tableColumnDatabaseContentsValutaDate: TableColumn<BankTransaction, LocalDate?>

    @FXML
    private lateinit var tableColumnDatabaseContentsSenderOrReceiver: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnDatabaseContentsIban: TableColumn<BankTransaction, IBAN?>

    @FXML
    private lateinit var tableColumnDatabaseContentsBic: TableColumn<BankTransaction, BIC?>

    @FXML
    private lateinit var tableColumnDatabaseContentsBookingText: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnDatabaseContentsUsageText: TableColumn<BankTransaction, String?>

    @FXML
    private lateinit var tableColumnDatabaseContentsCategory: TableColumn<BankTransaction, TransactionCategory?>

    @FXML
    private lateinit var tableColumnDatabaseContentsTags: TableColumn<BankTransaction, String>

    @FXML
    private lateinit var tableColumnDatabaseContentsAmount: TableColumn<BankTransaction, CurrencyAmount>

    private val stage
        get() = EntryClass.instance?.currentStage

    @FXML
    fun initialize() {
        choiceBoxImportSource.items = FXCollections.observableArrayList(*ImportSource.values())
        choiceBoxImportSource.selectionModel.select(0)

        textFieldDatabaseLocation.textProperty()
            .bindAndMap(MemoryDataHolder.currentDatabaseFile) { it?.absolutePath ?: "" }

        labelDatabaseCount.text = MemoryDataHolder.databaseContents.size.toString()
        MemoryDataHolder.databaseContents.addListener(ListChangeListener { change ->
            labelDatabaseCount.text = change.list.size.toString()
        })

        tableViewTransactionsToBeImported.items = MemoryDataHolder.transactionsToBeImported
        tableViewDatabaseContents.items = MemoryDataHolder.databaseContents

        tableColumnTransactionsToBeImportedBookingDate.cellValueFactory = PropertyValueFactory("bookingDate")
        tableColumnTransactionsToBeImportedValutaDate.cellValueFactory = PropertyValueFactory("valutaDate")
        tableColumnTransactionsToBeImportedSenderOrReceiver.cellValueFactory = PropertyValueFactory("senderOrReceiver")
        tableColumnTransactionsToBeImportedIban.cellValueFactory = PropertyValueFactory("iban")
        tableColumnTransactionsToBeImportedBic.cellValueFactory = PropertyValueFactory("bic")
        tableColumnTransactionsToBeImportedBookingText.cellValueFactory = PropertyValueFactory("bookingText")
        tableColumnTransactionsToBeImportedUsageText.cellValueFactory = PropertyValueFactory("usageText")
        tableColumnTransactionsToBeImportedCategory.cellValueFactory = PropertyValueFactory("category")
        tableColumnTransactionsToBeImportedTags.cellValueFactory = PropertyValueFactory("tags")
        tableColumnTransactionsToBeImportedAmount.cellValueFactory = PropertyValueFactory("amount")
        tableColumnDatabaseContentsBookingDate.cellValueFactory = PropertyValueFactory("bookingDate")
        tableColumnDatabaseContentsValutaDate.cellValueFactory = PropertyValueFactory("valutaDate")
        tableColumnDatabaseContentsSenderOrReceiver.cellValueFactory = PropertyValueFactory("senderOrReceiver")
        tableColumnDatabaseContentsIban.cellValueFactory = PropertyValueFactory("iban")
        tableColumnDatabaseContentsBic.cellValueFactory = PropertyValueFactory("bic")
        tableColumnDatabaseContentsBookingText.cellValueFactory = PropertyValueFactory("bookingText")
        tableColumnDatabaseContentsUsageText.cellValueFactory = PropertyValueFactory("usageText")
        tableColumnDatabaseContentsCategory.cellValueFactory = PropertyValueFactory("category")
        tableColumnDatabaseContentsTags.cellValueFactory = PropertyValueFactory("tags")
        tableColumnDatabaseContentsAmount.cellValueFactory = PropertyValueFactory("amount")
    }

    @FXML
    fun buttonChooseImportFileOnAction() {
        choiceBoxImportSource.selectionModel.selectedItem?.newUserFlow?.invoke()?.let {
            it.initiateImportUserFlow(stage!!)
            val transactions = it.parse()

            val similarityMatrix = transactions.calculateSimilarities()
            val mostSimilarTransactions = similarityMatrix.mapValues { entry ->
                entry.value.maxByOrNull { columnEntry -> columnEntry.value }!!
            }
                .toList()
                .sortedBy { pair -> pair.second.value }
            mostSimilarTransactions.forEach { (left, right) ->
                println("${left.usageText} - ${right.key.usageText} -- ${right.value}")
            }
            println("Done")

            MemoryDataHolder.transactionsToBeImported.clear()
            MemoryDataHolder.transactionsToBeImported.addAll(transactions)
        }
    }

    @FXML
    fun buttonNewDatabaseOnAction() {
        MemoryDataHolder.currentDatabaseFile.value = FileChooser().apply {
            title = "Select database location..."
            extensionFilters.clear()
            extensionFilters.add(FileChooser.ExtensionFilter("SQLite database", "*.$dbFileExtension"))
        }.showSaveDialog(stage) ?: return
    }

    @FXML
    fun buttonChooseDatabaseOnAction() {
        MemoryDataHolder.currentDatabaseFile.value = FileChooser().apply {
            title = "Select database location..."
            extensionFilters.clear()
            extensionFilters.add(FileChooser.ExtensionFilter("SQLite database", "*.$dbFileExtension"))
        }.showOpenDialog(stage) ?: return
    }

    @FXML
    fun buttonImportOnAction() {
        DatabaseManager.import(MemoryDataHolder.transactionsToBeImported)
        MemoryDataHolder.updateDatabaseContents()
        MemoryDataHolder.transactionsToBeImported.clear()
    }
}
