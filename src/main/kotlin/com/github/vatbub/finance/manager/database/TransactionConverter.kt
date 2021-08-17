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
package com.github.vatbub.finance.manager.database

import com.github.vatbub.finance.manager.BankTransaction
import com.github.vatbub.finance.manager.CurrencyAmount
import com.github.vatbub.finance.manager.database.BankTransactions.amount
import com.github.vatbub.finance.manager.database.BankTransactions.bic
import com.github.vatbub.finance.manager.database.BankTransactions.bookingDate
import com.github.vatbub.finance.manager.database.BankTransactions.bookingText
import com.github.vatbub.finance.manager.database.BankTransactions.category
import com.github.vatbub.finance.manager.database.BankTransactions.currency
import com.github.vatbub.finance.manager.database.BankTransactions.iban
import com.github.vatbub.finance.manager.database.BankTransactions.id
import com.github.vatbub.finance.manager.database.BankTransactions.senderOrReceiver
import com.github.vatbub.finance.manager.database.BankTransactions.usageText
import com.github.vatbub.finance.manager.database.BankTransactions.valutaDate
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun ResultRow.toMemoryTransaction(): BankTransaction = BankTransaction(
    this[bookingDate]?.toMemoryDate(),
    this[valutaDate]?.toMemoryDate(),
    this[senderOrReceiver],
    this[iban],
    this[bic],
    this[bookingText],
    this[usageText],
    this[category],
    DatabaseManager.getTagsFor(this[id].value),
    CurrencyAmount(this[amount], this[currency])
)

fun BankTransaction.toDatabaseBankTransaction(statement: InsertStatement<EntityID<Int>>) = statement.also {
    it[BankTransactions.bookingDate] = this.bookingDate?.toDatabaseDate()
    it[BankTransactions.valutaDate] = this.valutaDate?.toDatabaseDate()
    it[BankTransactions.senderOrReceiver] = this.senderOrReceiver
    it[BankTransactions.iban] = this.iban
    it[BankTransactions.bic] = this.bic
    it[BankTransactions.bookingText] = this.bookingText
    it[BankTransactions.usageText] = this.usageText
    it[BankTransactions.category] = this.category
    it[BankTransactions.amount] = this.amount.amount
    it[currency] = this.amount.currency
}

private val dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
private fun LocalDate.toDatabaseDate() = this.format(dateFormatter)
private fun String.toMemoryDate() = LocalDate.from(dateFormatter.parse(this))
