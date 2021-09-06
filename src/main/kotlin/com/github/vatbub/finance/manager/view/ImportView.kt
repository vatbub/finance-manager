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

import com.github.vatbub.finance.manager.database.MemoryDataHolder
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.BankTransaction
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.StringConverter
import java.net.URL
import java.util.*


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

    private lateinit var transactionsToBeImported: List<BankTransaction>

    private val transactionTableView by lazy {
        BankTransactionTableView(FXCollections.observableArrayList(transactionsToBeImported))
    }

    private lateinit var stage:Stage

    @FXML
    fun initialize() {
        MemoryDataHolder.currentInstance.addListener { _, _, newHolder ->
            memoryDataHolderChanged(newHolder)
        }

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
    }

    @FXML
    fun buttonEditAccountsOnAction() {
        AccountEditView.show()
    }

    @FXML
    fun buttonImportOnAction() {
        val selectedAccount = comboBoxDestinationAccount.selectionModel.selectedItem
            ?: throw IllegalStateException("Please select an account to import from")
        selectedAccount.import(transactionsToBeImported)
        stage.hide()
    }

    private fun memoryDataHolderChanged(memoryDataHolder: MemoryDataHolder = MemoryDataHolder.currentInstance.value) {
        val currentIndex = comboBoxDestinationAccount.selectionModel.selectedIndex.let {
            if (it < 0) 0 else it
        }
        comboBoxDestinationAccount.items = FXCollections.observableArrayList(memoryDataHolder.accountList)
        comboBoxDestinationAccount.selectionModel.select(currentIndex)
    }
}
