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

import com.github.vatbub.finance.manager.database.MemoryDataHolder
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.util.StringStringConverter
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.Stage
import javafx.util.Callback


class AccountEditView {
    companion object {
        fun show(): AccountEditView {
            val stage = Stage()

            val fxmlLoader = FXMLLoader(AccountEditView::class.java.getResource("AccountEditView.fxml"), null)
            val root = fxmlLoader.load<Parent>()
            val accountEditView = fxmlLoader.getController<AccountEditView>()

            val scene = Scene(root)
            stage.title = "Finance manager: Edit accounts"
            // val iconName = "icon.png"
            // primaryStage.icons.add(Image(javaClass.getResourceAsStream(iconName)))

            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()

            return accountEditView
        }
    }

    @FXML
    private lateinit var tableViewAccounts: TableView<Account>

    @FXML
    private lateinit var tableColumnAccountName: TableColumn<Account, String>

    @FXML
    private lateinit var tableColumnAccountBalance: TableColumn<Account, Number>

    @FXML
    private lateinit var tableColumnControls: TableColumn<Account, *>

    @FXML
    fun initialize() {
        MemoryDataHolder.currentInstance.addListener { _, _, newHolder ->
            memoryDataHolderChanged(newHolder)
        }

        tableColumnAccountName.cellValueFactory = Account::name.cellValueFactory()
        tableColumnAccountBalance.cellValueFactory = Account::balance.cellValueFactory()

        tableColumnAccountName.cellFactory = Callback {
            object : ObjectEditingCell<Account, String>(StringStringConverter) {
                override fun updateItemPropertyValue(item: Account, newValue: String) {
                    item.name.value = newValue
                }
            }
        }

        tableColumnControls.cellFactory = Callback { DeleteButtonCell() }

        memoryDataHolderChanged()
    }

    @FXML
    fun buttonAddAccountOnAction() {
        MemoryDataHolder.currentInstance.value.accountList.let {
            val accountNumber = it.size + 1
            it.add(Account("Account $accountNumber", listOf()))
        }
    }

    private fun memoryDataHolderChanged(memoryDataHolder: MemoryDataHolder = MemoryDataHolder.currentInstance.value) {
        tableViewAccounts.items = memoryDataHolder.accountList
    }
}
