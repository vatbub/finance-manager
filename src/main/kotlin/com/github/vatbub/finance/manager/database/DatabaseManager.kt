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
import com.github.vatbub.finance.manager.database.BankTransactionsToTagsRelation.tagId
import com.github.vatbub.finance.manager.database.BankTransactionsToTagsRelation.transactionId
import com.github.vatbub.finance.manager.database.Tags.tag
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseManager {
    fun connect(database: File/*, password: String*/) {
        val connectionString = "jdbc:sqlite:" +
                database.absolutePath
                    .replace("\\", "/")

        Database.connect(connectionString, driver = "org.sqlite.JDBC")
    }

    fun initializeScheme() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())
            commit()
        }
    }

    fun getAllBankTransactions() = transaction {
        Internal.getAllBankTransactions()
    }

    fun getTagsFor(id: Int): List<String> = transaction {
        val relationQuery = BankTransactionsToTagsRelation.select {
            transactionId eq id
        }
        relationQuery.map { resultRow ->
            Tags.select { Tags.id eq resultRow[tagId] }.first()[tag]
        }
    }

    fun import(bankTransactions: List<BankTransaction>) = transaction {
        val currentDatabaseContents = Internal.getAllBankTransactions()
        bankTransactions.filterNot { bankTransaction -> currentDatabaseContents.contains(bankTransaction) }
            .forEach { bankTransaction ->
                val tagIds = Internal.insertTagsAndGetTagIds(bankTransaction.tags)
                val bankTransactionId = BankTransactions.insertAndGetId {
                    bankTransaction.toDatabaseBankTransaction(it)
                }.value
                Internal.storeTagRelation(bankTransactionId, tagIds)
            }
    }

    private object Internal {
        fun getAllBankTransactions() =
            BankTransactions.selectAll().map { it.toMemoryTransaction() }

        fun insertTagsAndGetTagIds(tags: List<String>) = tags.map { tag ->
            getTagIdIfExists(tag) ?: Tags.insertAndGetId {
                it[Tags.tag] = tag
            }.value
        }

        fun getTagIdIfExists(tag: String): Int? {
            val query = Tags.select { Tags.tag eq tag }
            if (query.count() == 0L) return null
            return query.first()[Tags.id].value
        }

        fun storeTagRelation(bankTransactionId: Int, tagIds: List<Int>) {
            tagIds.forEach { tagId ->
                BankTransactionsToTagsRelation.insert {
                    it[BankTransactionsToTagsRelation.tagId] = tagId
                    it[transactionId] = bankTransactionId
                }
            }
        }
    }
}
