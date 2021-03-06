/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2016 - 2021 Frederik Kammel
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
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.layout.HBox

class DeleteButtonCell : TableCell<Account, Account>() {
    private val deleteButton by lazy {
        Button("Delete").also {
            // it.graphic = ImageView(Image(javaClass.getResourceAsStream("up-arrow.png")))
            it.setOnAction(this::deleteButtonOnAction)
        }
    }

    private val hBox by lazy {
        HBox(
            deleteButton,
        ).apply {
            spacing = 8.0
        }
    }


    override fun updateItem(item: Account?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
            return
        }

        graphic = hBox

        Platform.runLater {
            if (tableColumn.width < hBox.width)
                tableColumn.prefWidth = hBox.width + 50
        }
    }

    private fun deleteButtonOnAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val account = tableRow.item ?: return
        MemoryDataHolder.currentInstance.value.accountList.remove(account)
    }
}
