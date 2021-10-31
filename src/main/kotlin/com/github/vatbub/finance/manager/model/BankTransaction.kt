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
package com.github.vatbub.finance.manager.model

import com.github.vatbub.finance.manager.calculations.similarityTo
import com.github.vatbub.finance.manager.calculations.weightOf
import com.github.vatbub.finance.manager.calculations.weightedAverage
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.time.LocalDate

class BankTransaction(
    val bookingDate: ObjectProperty<LocalDate>,
    val valutaDate: ObjectProperty<LocalDate>,
    val senderOrReceiver: StringProperty,
    val iban: ObjectProperty<IBAN>,
    val bic: ObjectProperty<BIC>,
    val bookingText: StringProperty,
    val usageText: StringProperty,
    val category: ObjectProperty<TransactionCategory>,
    val tags: ObservableList<String>,
    val amount: ObjectProperty<CurrencyAmount>,
    val recurringBankTransaction: ObjectProperty<RecurringBankTransaction>,
    val selected: BooleanProperty
) : ObservableWithObservableProperties, ObservableWithObservableListProperties {
    constructor(
        bookingDate: LocalDate?,
        valutaDate: LocalDate?,
        senderOrReceiver: String?,
        iban: IBAN?,
        bic: BIC?,
        bookingText: String?,
        usageText: String?,
        category: TransactionCategory?,
        tags: List<String>,
        amount: CurrencyAmount,
        recurringBankTransaction: RecurringBankTransaction? = null,
        selected: Boolean = true
    ) : this(
        SimpleObjectProperty<LocalDate>(bookingDate),
        SimpleObjectProperty<LocalDate>(valutaDate),
        SimpleStringProperty(senderOrReceiver),
        SimpleObjectProperty<IBAN>(iban),
        SimpleObjectProperty<BIC>(bic),
        SimpleStringProperty(bookingText),
        SimpleStringProperty(usageText),
        SimpleObjectProperty<TransactionCategory>(category),
        FXCollections.observableArrayList(tags),
        SimpleObjectProperty<CurrencyAmount>(amount),
        SimpleObjectProperty(recurringBankTransaction),
        SimpleBooleanProperty(selected)
    )

    infix fun similarityTo(other: BankTransaction): Double = listOf(
        0.2 weightOf senderOrReceiver.value.similarityTo(other.senderOrReceiver.value),
        0.2 weightOf iban.value.similarityTo(other.iban.value),
        0.2 weightOf bic.value.similarityTo(other.bic.value),
        0.2 weightOf bookingText.value.similarityTo(other.bookingText.value),
        0.3 weightOf usageText.value.similarityTo(other.usageText.value),
        0.1 weightOf category.value.similarityTo(other.category.value),
        0.1 weightOf tags.similarityTo(other.tags),
        0.2 weightOf amount.value.similarityTo(other.amount.value)
    ).weightedAverage()

    override val observableProperties: List<ObservableValue<*>> = listOf(
        bookingDate,
        valutaDate,
        senderOrReceiver,
        iban,
        bic,
        bookingText,
        usageText,
        category,
        amount,
        recurringBankTransaction
    )
    override val observableLists: List<ObservableList<*>> = listOf(tags)

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BankTransaction) return false

        if (bookingDate != other.bookingDate) return false
        if (valutaDate != other.valutaDate) return false
        if (senderOrReceiver != other.senderOrReceiver) return false
        if (iban != other.iban) return false
        if (bic != other.bic) return false
        if (bookingText != other.bookingText) return false
        if (usageText != other.usageText) return false
        if (category != other.category) return false
        if (tags != other.tags) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bookingDate.value?.hashCode() ?: 0
        result = 31 * result + (valutaDate.value?.hashCode() ?: 0)
        result = 31 * result + (senderOrReceiver.value?.hashCode() ?: 0)
        result = 31 * result + (iban.value?.hashCode() ?: 0)
        result = 31 * result + (bic.value?.hashCode() ?: 0)
        result = 31 * result + (bookingText.value?.hashCode() ?: 0)
        result = 31 * result + (usageText.value?.hashCode() ?: 0)
        result = 31 * result + (category.value?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        result = 31 * result + amount.value.hashCode()
        return result
    }
}

typealias IBAN = String
typealias BIC = String
