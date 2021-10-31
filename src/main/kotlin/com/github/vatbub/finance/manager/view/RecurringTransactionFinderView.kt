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
import com.github.vatbub.finance.manager.database.MemoryDataHolder
import com.github.vatbub.finance.manager.logging.logger
import com.github.vatbub.finance.manager.model.BankTransaction
import com.github.vatbub.finance.manager.model.RecurringBankTransaction
import com.github.vatbub.finance.manager.model.findRecurringTransactions
import com.github.vatbub.finance.manager.util.bindAndMap
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.Stage


class RecurringTransactionFinderView {
    companion object {
        private var currentSuggestionIndex = 0
        private val calculatedSuggestions = SimpleObjectProperty<List<List<BankTransaction>>>(null)

        fun showWithNextSuggestion(doRecalculation: Boolean = true, useCachedSimilarities: Boolean = false) {
            if (!doRecalculation && calculatedSuggestions.value != null) {
                if (currentSuggestionIndex + 1 >= calculatedSuggestions.value.size) {
                    logger.warn("These are all the patterns that I found.")
                    return
                }

                currentSuggestionIndex++
                show(calculatedSuggestions.value[currentSuggestionIndex])
                return
            }

            val searchCallable =
                MemoryDataHolder.currentInstance.value
                    ?.accountList
                    ?.map { it.transactions }
                    ?.flatten()
                    ?.findRecurringTransactions(useCachedSimilarities = useCachedSimilarities)
                    ?: throw NullPointerException("Cannot find duplicates: MemoryDataHolder not initialized.")
            searchCallable.valueProperty().addListener { _, _, newValue ->
                Platform.runLater {
                    if (!useCachedSimilarities)
                        currentSuggestionIndex = 0

                    calculatedSuggestions.value = newValue
                    show(newValue[currentSuggestionIndex])
                }
            }

            BackgroundScheduler.singleThreaded.enqueue(searchCallable)
        }

        private fun show(bankTransactions: List<BankTransaction>): RecurringTransactionFinderView {
            val stage = Stage()

            val fxmlLoader = FXMLLoader(
                RecurringTransactionFinderView::class.java.getResource("RecurringTransactionFinderView.fxml"),
                null
            )
            val root = fxmlLoader.load<Parent>()
            val recurringTransactionFinderView = fxmlLoader.getController<RecurringTransactionFinderView>().apply {
                transactionsToBeCombined.value = bankTransactions
                initTransactionTableView()
            }

            recurringTransactionFinderView.stage = stage

            val scene = Scene(root)
            stage.isMaximized = true
            stage.title = "Finance manager: Find recurring transactions"
            // val iconName = "icon.png"
            // primaryStage.icons.add(Image(javaClass.getResourceAsStream(iconName)))

            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()

            return recurringTransactionFinderView
        }
    }

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var textFieldDescription: TextField

    private val transactionsToBeCombined = SimpleObjectProperty<List<BankTransaction>>(listOf())

    private val prevailingRecurringTransaction = SimpleObjectProperty(RecurringBankTransaction(""))

    private val transactionTableView by lazy {
        BankTransactionTableView(FXCollections.observableArrayList(transactionsToBeCombined.value)).also {
            it.isEditable = true
        }
    }

    private lateinit var stage: Stage

    @FXML
    fun initialize() {
        textFieldDescription.textProperty().bindAndMap(prevailingRecurringTransaction) {
            it?.description?.value
        }
        prevailingRecurringTransaction.bindAndMap(transactionsToBeCombined) { transactions ->
            transactions?.groupBy { it.recurringBankTransaction.value }
                ?.filterKeys { it != null }
                ?.maxByOrNull { it.value.size }
                ?.key ?: RecurringBankTransaction("")
        }

        Platform.runLater { textFieldDescription.requestFocus() }
    }

    private fun initTransactionTableView() {
        rootPane.children.add(transactionTableView)
        GridPane.setRowIndex(transactionTableView, 1)
        GridPane.setColumnSpan(transactionTableView, GridPane.REMAINING)
        GridPane.setVgrow(transactionTableView, Priority.ALWAYS)
        GridPane.setHgrow(transactionTableView, Priority.ALWAYS)
    }

    @FXML
    fun buttonCancelAction() {
        stage.hide()
    }

    @FXML
    fun buttonSaveAction() {
        val recurringBankTransaction = prevailingRecurringTransaction.value
            ?: throw NullPointerException("PrevailingRecurringTransaction was null")
        recurringBankTransaction.description.value = textFieldDescription.text
        transactionsToBeCombined.value
            .filter { it.selected.value }
            .forEach { it.recurringBankTransaction.value = recurringBankTransaction }
        stage.hide()

        showWithNextSuggestion(useCachedSimilarities = true)
    }

    @FXML
    fun buttonSkipAction() {
        stage.hide()
        showWithNextSuggestion(doRecalculation = false)
    }
}
