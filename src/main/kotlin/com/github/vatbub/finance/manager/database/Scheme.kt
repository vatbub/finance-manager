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

import com.github.vatbub.finance.manager.model.Currency
import com.github.vatbub.finance.manager.model.TransactionCategory
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object BankTransactions : IntIdTable() {
    val accountId = integer("accountId").references(Accounts.id)
    val bookingDate = char("bookingDate", 8).nullable()
    val valutaDate = char("valutaDate", 8).nullable()
    val senderOrReceiver = varchar("senderOrReceiver", 70).nullable()
    val iban = varchar("iban", 32).nullable()
    val bic = varchar("bic", 11).nullable()
    val bookingText = varchar("bookingText", 33).nullable()
    val usageText = varchar("usageText", 140).nullable()
    val category = enumeration("category", TransactionCategory::class).nullable()
    val amount = double("amount")
    val currency = enumeration("currency", Currency::class)
}

object Tags : IntIdTable() {
    val tag = varchar("tag", 50)
}

object BankTransactionsToTagsRelation : Table() {
    val transactionId = integer("transactionId").references(BankTransactions.id)
    val tagId = integer("tagId").references(Tags.id)

    override val primaryKey = PrimaryKey(transactionId, tagId)
}

object Accounts: IntIdTable() {
    val name = varchar("name", 100)
}

object Preferences:Table() {
    val key = varchar("key", 100)
    val value = varchar("value", 100)
    override val primaryKey = PrimaryKey(key)
}

val financialTables = listOf(BankTransactions, Tags, BankTransactionsToTagsRelation, Accounts)
val nonFinancialTables = listOf(Preferences)
val tables = financialTables + nonFinancialTables
