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
package com.github.vatbub.finance.manager.view

import com.github.vatbub.finance.manager.ConsorsbankCsvParser
import com.github.vatbub.finance.manager.EntryClass
import com.github.vatbub.finance.manager.database.DatabaseManager
import com.github.vatbub.finance.manager.database.DatabaseUpdateListener
import com.github.vatbub.finance.manager.database.PreferenceKeys.MainView.LastTimeWindowAmount
import com.github.vatbub.finance.manager.database.PreferenceKeys.MainView.LastTimeWindowUnit
import com.github.vatbub.finance.manager.database.preferences
import com.github.vatbub.finance.manager.dbFileExtension
import com.github.vatbub.finance.manager.model.CurrencyAmount
import com.github.vatbub.finance.manager.model.TransactionCategory
import com.github.vatbub.finance.manager.util.bindAndMap
import com.github.vatbub.finance.manager.util.isWithinRange
import com.github.vatbub.finance.manager.view.AccountDisplayTimeUnit.*
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.PieChart
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.util.converter.LongStringConverter
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.util.*
import kotlin.math.abs
import kotlin.system.exitProcess


class MainView : DatabaseUpdateListener {
    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var rootPane: VBox

    @FXML
    private lateinit var textFieldLastTimeWindowAmount: TextField

    @FXML
    private lateinit var comboBoxLastTimeWindowUnit: ComboBox<AccountDisplayTimeUnit>

    @FXML
    private lateinit var labelLastEarnings: Label

    @FXML
    private lateinit var labelLastSpendings: Label

    @FXML
    private lateinit var tableViewCurrentAccountBalances: TableView<*>

    @FXML
    private lateinit var tableColumnAccountNames: TableColumn<*, *>

    @FXML
    private lateinit var tableColumnAccountBalances: TableColumn<*, *>

    @FXML
    private lateinit var pieChartSpendingsPerCategory: PieChart

    @FXML
    private lateinit var importMenu: Menu

    @FXML
    private lateinit var editAccountsMenuItem: MenuItem

    @FXML
    private lateinit var editDataMenuItem: MenuItem


    @FXML
    fun aboutAction() {
        TODO("About screen not implemented yet")
    }

    @FXML
    fun closeProgramAction() {
        exitProcess(0)
    }

    @FXML
    fun editDataAction() {
        TODO("Edit data action not yet implemented")
    }

    @FXML
    fun editAccountsAction() {
        AccountEditView.show()
    }

    @FXML
    fun importConsorsbankCsvAction() {
        val fileChooser = FileChooser().apply {
            title = "Choose Consorsbank CSV file..."
            initialDirectory = File(".")
        }
        val sourceFile = fileChooser.showOpenDialog(stage)

        // TODO replace with background scheduler
        val bankTransactions = ConsorsbankCsvParser(sourceFile)

        ImportView.show(bankTransactions)
    }

    @FXML
    fun newDatabaseAction() {
        DatabaseManager.currentDatabaseFile.value = FileChooser().apply {
            title = "Select database location..."
            extensionFilters.clear()
            extensionFilters.add(FileChooser.ExtensionFilter("SQLite database", "*.$dbFileExtension"))
        }.showSaveDialog(stage) ?: return
    }

    @FXML
    fun openDatabaseAction() {
        DatabaseManager.currentDatabaseFile.value = FileChooser().apply {
            title = "Select database location..."
            extensionFilters.clear()
            extensionFilters.add(FileChooser.ExtensionFilter("SQLite database", "*.$dbFileExtension"))
        }.showOpenDialog(stage) ?: return
    }

    private val stage
        get() = EntryClass.instance?.currentStage

    @FXML
    fun initialize() {
        DatabaseManager.updateListeners.add(this)

        importMenu.disableProperty().bindAndMap(DatabaseManager.currentDatabaseFile) { it == null }
        editAccountsMenuItem.disableProperty().bindAndMap(DatabaseManager.currentDatabaseFile) { it == null }
        editDataMenuItem.disableProperty().bindAndMap(DatabaseManager.currentDatabaseFile) { it == null }

        comboBoxLastTimeWindowUnit.items = FXCollections.observableArrayList(*values())
        comboBoxLastTimeWindowUnit.selectionModel.select(preferences[LastTimeWindowUnit])
        comboBoxLastTimeWindowUnit.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            preferences[LastTimeWindowUnit] = newValue
            updateLastAmountLabels(lastTimeWindowUnit = newValue)
        }

        textFieldLastTimeWindowAmount.textFormatter = TextFormatter(LongStringConverter())
        textFieldLastTimeWindowAmount.text = preferences[LastTimeWindowAmount].toString()
        textFieldLastTimeWindowAmount.textProperty().addListener { _, _, newValue ->
            val longValue = newValue.toLongOrNull() ?: return@addListener
            preferences[LastTimeWindowAmount] = longValue
            updateLastAmountLabels(lastTimeWindowAmount = longValue)
        }

        updateLastAmountLabels()

        tableColumnAccountNames.cellValueFactory = PropertyValueFactory("name")
        tableColumnAccountBalances.cellValueFactory = PropertyValueFactory("balance")

        DatabaseManager.currentDatabaseFile.addListener { _, _, _ ->
            databaseUpdateHappened()
        }
    }

    private fun updateLastAmountLabels(
        lastTimeWindowAmount: Long = textFieldLastTimeWindowAmount.text.toLong(),
        lastTimeWindowUnit: AccountDisplayTimeUnit = comboBoxLastTimeWindowUnit.value
    ) {
        if (!DatabaseManager.isConnected) return

        val now = LocalDate.now()
        val relevantTimeWindowStart = when (lastTimeWindowUnit) {
            Days -> now.minusDays(lastTimeWindowAmount)
            Months -> now.minusMonths(lastTimeWindowAmount - 1).withDayOfMonth(1)
            Years -> now.minusYears(lastTimeWindowAmount - 1).withMonth(1).withDayOfMonth(1)
        }

        val relevantTransactions = DatabaseManager.getAllAccounts()
            .map { it.transactions }
            .flatten()
            .filter { it.valutaDate?.isWithinRange(relevantTimeWindowStart, now) ?: false }
            .map { it.amount.amount }

        labelLastEarnings.text = relevantTransactions
            .filter { it >= 0 }
            .sum()
            .toString() + " €"
        labelLastSpendings.text = relevantTransactions
            .filter { it < 0 }
            .sum()
            .let { abs(it) }
            .toString() + " €"
    }

    private data class CategoryAndAmount(val category: TransactionCategory, val amount: CurrencyAmount)

    private fun updatePieChart() {
        pieChartSpendingsPerCategory.data = DatabaseManager.getAllAccounts()
            .map { it.transactions }
            .flatten()
            .filter { it.amount.amount <= 0 }
            .mapNotNull { it.category?.let { category -> CategoryAndAmount(category, it.amount) } }
            .groupBy { it.category }
            .map { entry ->
                val totalAmount = entry.value
                    .sumOf { it.amount.amount }
                    .let { abs(it) }
                PieChart.Data(entry.key.toString(), totalAmount)
            }
            .let { FXCollections.observableArrayList(it) }
    }

    override fun databaseUpdateHappened() {
        updateLastAmountLabels()
        tableViewCurrentAccountBalances.items = FXCollections.observableArrayList(DatabaseManager.getAllAccounts())
        updatePieChart()
    }
}