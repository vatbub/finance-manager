package com.github.vatbub.finance.manager.view

import com.github.vatbub.finance.manager.database.DatabaseManager
import com.github.vatbub.finance.manager.database.DatabaseUpdateListener
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.util.StringStringConverter
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import javafx.util.Callback


class AccountEditView:DatabaseUpdateListener {
    companion object{
        fun show():AccountEditView{
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
    private lateinit var tableColumnAccountBalance: TableColumn<Account, Double>

    @FXML
    private lateinit var tableColumnControls: TableColumn<Account, *>

    @FXML
    fun initialize() {
        DatabaseManager.updateListeners.add(this)

        tableColumnAccountName.cellValueFactory = PropertyValueFactory("name")
        tableColumnAccountBalance.cellValueFactory = PropertyValueFactory("balance")

        tableColumnAccountName.cellFactory = Callback {
            object : ObjectEditingCell<Account, String>(StringStringConverter) {
                override fun updateItemPropertyValue(item: Account, newValue: String) {
                    DatabaseManager.updateAccount(item.id!!, newValue)
                }
            }
        }

        tableColumnControls.cellFactory = Callback { DeleteButtonCell() }

        databaseUpdateHappened()
    }

    @FXML
    fun buttonAddAccountOnAction() {
        val accountNumber = DatabaseManager.getAllAccounts().size + 1
        DatabaseManager.createAccount(Account("Account $accountNumber", listOf()))
    }

    override fun databaseUpdateHappened() {
        tableViewAccounts.items = FXCollections.observableArrayList(DatabaseManager.getAllAccounts())
    }
}
