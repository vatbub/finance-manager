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

import com.github.vatbub.finance.manager.BackgroundScheduler
import com.github.vatbub.finance.manager.RunnableWithProgressUpdates
import com.github.vatbub.finance.manager.database.MemoryDataHolder
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.BankTransaction
import com.github.vatbub.finance.manager.util.parallelForEach
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.StringConverter
import java.net.URL
import java.util.*
import kotlin.properties.Delegates


class ImportView {
    companion object {
        fun show(bankTransactions: List<BankTransaction>): ImportView {
            val stage = Stage()

            val fxmlLoader = FXMLLoader(ImportView::class.java.getResource("ImportView.fxml"), null)
            val root = fxmlLoader.load<Parent>()
            val importView = fxmlLoader.getController<ImportView>().apply {
                transactionsToBeImported = bankTransactions
                initTransactionTableView()
            }

            importView.stage = stage

            val scene = Scene(root)
            stage.title = "Finance manager: Import"
            // val iconName = "icon.png"
            // primaryStage.icons.add(Image(javaClass.getResourceAsStream(iconName)))

            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()

            return importView
        }
    }

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var vboxWithTransactionTable: VBox

    @FXML
    private lateinit var comboBoxDestinationAccount: ComboBox<Account>

    @FXML
    private lateinit var buttonImport: Button

    @FXML
    private lateinit var labelWaitForBackgroundTasks: Label

    private var transactionsToBeImported: List<BankTransaction> by Delegates.observable(listOf()) { _, _, newValue ->
        deselectDuplicatesAndIncompleteTransactions(newValue)
    }

    private val transactionTableView by lazy {
        BankTransactionTableView(FXCollections.observableArrayList(transactionsToBeImported)).also {
            it.isEditable = true
        }
    }

    private val duplicateDetectionInProgress = SimpleBooleanProperty(false)

    private lateinit var stage: Stage

    @FXML
    fun initialize() {
        MemoryDataHolder.currentInstance.addListener { _, _, newHolder ->
            memoryDataHolderChanged(newHolder)
        }

        buttonImport.disableProperty().bind(duplicateDetectionInProgress)
        labelWaitForBackgroundTasks.visibleProperty().bind(duplicateDetectionInProgress)

        comboBoxDestinationAccount.converter = object : StringConverter<Account>() {
            override fun toString(`object`: Account?): String = `object`?.name?.value ?: "<null>"

            override fun fromString(string: String?): Account {
                throw NotImplementedError()
            }
        }

        memoryDataHolderChanged()
    }

    private fun initTransactionTableView() {
        vboxWithTransactionTable.children.add(transactionTableView)
        VBox.setVgrow(transactionTableView, Priority.ALWAYS)
    }

    @FXML
    fun buttonEditAccountsOnAction() {
        AccountEditView.show()
    }

    @FXML
    fun buttonImportOnAction() {
        val selectedAccount = comboBoxDestinationAccount.selectionModel.selectedItem
            ?: throw IllegalStateException("Please select an account to import to")
        BackgroundScheduler.singleThreaded.enqueue(message = "Importing data...") {
            selectedAccount.import(transactionsToBeImported)
        }
        stage.hide()
    }

    private fun memoryDataHolderChanged(memoryDataHolder: MemoryDataHolder = MemoryDataHolder.currentInstance.value) {
        val currentIndex = comboBoxDestinationAccount.selectionModel.selectedIndex.let {
            if (it < 0) 0 else it
        }
        comboBoxDestinationAccount.items = memoryDataHolder.accountList
        comboBoxDestinationAccount.selectionModel.select(currentIndex)
    }

    private fun deselectDuplicatesAndIncompleteTransactions(transactionsToImport: List<BankTransaction>) {
        val allTransactions = MemoryDataHolder.currentInstance.value
            .accountList
            .map { it.transactions }
            .flatten()

        BackgroundScheduler.multiThreaded.enqueue(
            RunnableWithProgressUpdates<Unit>(
                taskMessage = "Finding duplicates...",
                totalSteps = allTransactions.size.toLong()
            ) {
                duplicateDetectionInProgress.value = true

                transactionsToImport.parallelForEach() { transactionToEvaluate ->
                    if (transactionToEvaluate.isIncomplete()) {
                        Platform.runLater { transactionToEvaluate.selected.value = false }
                        return@parallelForEach
                    }

                    if (allTransactions.isEmpty()) return@parallelForEach

                    val maxSimilarity = allTransactions
                        .filter { transactionToEvaluate.bookingDate.value == it.bookingDate.value }
                        .filter { transactionToEvaluate.valutaDate.value == it.valutaDate.value }
                        .maxOf { it similarityTo transactionToEvaluate }

                    stepDone()

                    if (maxSimilarity < 0.999) return@parallelForEach
                    Platform.runLater { transactionToEvaluate.selected.value = false }
                }

                duplicateDetectionInProgress.value = false
            })
    }

    private fun BankTransaction.isIncomplete(): Boolean {
        return bookingDate.value == null ||
                valutaDate.value == null
    }
}
