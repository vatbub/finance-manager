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
import java.time.LocalDate

data class BankTransaction(
    val bookingDate: LocalDate?,
    val valutaDate: LocalDate?,
    val senderOrReceiver: String?,
    val iban: IBAN?,
    val bic: BIC?,
    val bookingText: String?,
    val usageText: String?,
    val category: TransactionCategory?,
    val tags: List<String>,
    val amount: CurrencyAmount,
    val id: Int? = null
) {
    infix fun similarityTo(other: BankTransaction): Double = listOf(
        0.2 weightOf senderOrReceiver.similarityTo(other.senderOrReceiver),
        0.2 weightOf iban.similarityTo(other.iban),
        0.2 weightOf bic.similarityTo(other.bic),
        0.2 weightOf bookingText.similarityTo(other.bookingText),
        0.3 weightOf usageText.similarityTo(other.usageText),
        0.1 weightOf category.similarityTo(other.category),
        0.1 weightOf tags.similarityTo(other.tags),
        0.2 weightOf amount.similarityTo(other.amount)
    ).weightedAverage()

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
        var result = bookingDate?.hashCode() ?: 0
        result = 31 * result + (valutaDate?.hashCode() ?: 0)
        result = 31 * result + (senderOrReceiver?.hashCode() ?: 0)
        result = 31 * result + (iban?.hashCode() ?: 0)
        result = 31 * result + (bic?.hashCode() ?: 0)
        result = 31 * result + (bookingText?.hashCode() ?: 0)
        result = 31 * result + (usageText?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }
}

typealias IBAN = String
typealias BIC = String

fun List<BankTransaction>.calculateSimilarities() = associateWith { transaction ->
    this.filter { otherTransaction -> transaction != otherTransaction }
        .associateWith { otherTransaction -> transaction similarityTo otherTransaction }
}