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

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.layout.GridPane
import java.net.URL
import java.util.*


class MainView {

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var choiceBoxImportSource: ChoiceBox<ImportSource>

    private val stage
        get() = EntryClass.instance?.currentStage

    @FXML
    fun buttonImportOnAction() {
        choiceBoxImportSource.selectionModel.selectedItem?.newUserFlow?.invoke()?.let {
            it.initiateImportUserFlow(stage!!)
            val transactions = it.parse()
            /*transactions.forEach { transaction ->
                println(
                    "${transaction.bookingName} - " +
                            "${transaction.valutaDate} - " +
                            "${transaction.senderOrReceiver} - " +
                            "${transaction.iban} - ${transaction.bic} - " +
                            "${transaction.bookingText} - " +
                            "${transaction.usageText} - " +
                            "${transaction.category} - " +
                            "${transaction.tags.joinToString(", ")} - " +
                            "${transaction.amount}"
                )
            }*/

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
        }
    }

    @FXML
    fun initialize() {
        choiceBoxImportSource.items = FXCollections.observableArrayList(*ImportSource.values())
        choiceBoxImportSource.selectionModel.select(0)
    }
}
