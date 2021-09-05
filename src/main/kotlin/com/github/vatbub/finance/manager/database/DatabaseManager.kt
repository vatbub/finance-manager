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

import com.github.vatbub.finance.manager.database.BankTransactionsToTagsRelation.tagId
import com.github.vatbub.finance.manager.database.BankTransactionsToTagsRelation.transactionId
import com.github.vatbub.finance.manager.database.Tags.tag
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.BankTransaction
import com.github.vatbub.kotlin.preferences.KeyValueProvider
import com.github.vatbub.kotlin.preferences.MemoryKeyValueProvider
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseManager : KeyValueProvider {
    val updateListeners = mutableSetOf<DatabaseUpdateListener>()

    val currentDatabaseFile: ObjectProperty<File> = SimpleObjectProperty<File>(null)
        .also {
            it.addListener { _, _, newValue ->
                if (newValue == null) {
                    isConnected = false
                    return@addListener
                }
                connect(newValue)
                initializeScheme()
            }
        }

    var isConnected = false
        private set

    private val memoryKeyValueProvider = MemoryKeyValueProvider()

    private fun connect(database: File/*, password: String*/) {
        val connectionString = "jdbc:sqlite:" +
                database.absolutePath
                    .replace("\\", "/")

        Database.connect(connectionString, driver = "org.sqlite.JDBC")
        if (!isConnected) flushPendingPreferenceOperations()
        isConnected = true
    }

    fun initializeScheme() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())
            commit()
        }
    }

    private fun fireUpdateHappened() = updateListeners.forEach { it.databaseUpdateHappened() }

    fun getAllAccounts() = transaction {
        Internal.getAllAccounts()
    }

    fun getTagsFor(id: Int): List<String> = transaction {
        val relationQuery = BankTransactionsToTagsRelation.select {
            transactionId eq id
        }
        relationQuery.map { resultRow ->
            Tags.select { Tags.id eq resultRow[tagId] }.first()[tag]
        }
    }

    fun import(account: Account, bankTransactions: List<BankTransaction>) = transaction {
        Internal.import(account.id!!, bankTransactions)
    }.also { fireUpdateHappened() }

    fun createAccount(account: Account): Account = transaction {
        Internal.createAccount(account)
    }.also { fireUpdateHappened() }

    fun deleteAccount(account: Account) = transaction {
        Internal.deleteAccount(account)
    }.also { fireUpdateHappened() }

    fun updateAccount(accountId: Int, newName: String) = transaction {
        Internal.updateAccount(accountId, newName)
    }.also { fireUpdateHappened() }

    override val isPersistent: Boolean = true

    override fun get(key: String): String? {
        if (!isConnected) return memoryKeyValueProvider[key]

        return transaction {
            Internal.getPreference(key)
        }
    }

    override fun set(key: String, value: String?) {
        if (!isConnected) {
            memoryKeyValueProvider[key] = value
            return
        }

        transaction {
            Internal.setPreference(key, value)
        }
    }

    private fun flushPendingPreferenceOperations() = transaction {
        val copyOfMemoryContents = memoryKeyValueProvider.contents.toList()
        memoryKeyValueProvider.contents.clear()

        copyOfMemoryContents.forEach { pair ->
            Internal.setPreference(pair.first, pair.second)
        }
    }

    private object Internal {
        fun getAllAccounts() =
            Accounts.selectAll().map {
                Account(
                    name = it[Accounts.name],
                    transactions = getAllBankTransactions(it[Accounts.id].value),
                    id = it[Accounts.id].value
                )
            }

        fun getAllBankTransactions(accountId: Int) =
            BankTransactions.select {
                BankTransactions.accountId eq accountId
            }.map { it.toMemoryTransaction() }

        fun import(accountId: Int, bankTransactions: List<BankTransaction>) {
            val currentDatabaseContents = Internal.getAllBankTransactions(accountId)
            bankTransactions.filterNot { bankTransaction -> currentDatabaseContents.contains(bankTransaction) }
                .forEach { bankTransaction ->
                    val tagIds = Internal.insertTagsAndGetTagIds(bankTransaction.tags)
                    val bankTransactionId = BankTransactions.insertAndGetId {
                        bankTransaction.toDatabaseBankTransaction(accountId, it)
                    }.value
                    storeTagRelation(bankTransactionId, tagIds)
                }
        }

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

        fun createAccount(account: Account): Account {
            val accountId = Accounts.insertAndGetId {
                it[name] = account.name
            }.value

            import(accountId, account.transactions)

            return account.copy(id = accountId)
        }

        fun deleteTransaction(bankTransaction: BankTransaction) {
            BankTransactions.deleteWhere { BankTransactions.id eq bankTransaction.id!! }
            BankTransactionsToTagsRelation.deleteWhere { transactionId eq bankTransaction.id!! }
        }

        fun deleteAccount(account: Account) {
            account.transactions.forEach {
                deleteTransaction(it)
            }
            Accounts.deleteWhere { Accounts.id eq account.id!! }
        }

        fun updateAccount(accountId: Int, newName: String){
            Accounts.update(where = {
                Accounts.id eq accountId
            }, body = {
                it[name] = newName
            })
        }

        fun getPreference(key: String): String? {
            return Preferences.select {
                Preferences.key eq key
            }.firstOrNull()?.get(Preferences.value)
        }

        fun setPreference(key: String, value: String?) {
            if (value == null) {
                deletePreference(key)
                return
            }

            if (getPreference(key) == null) {
                insertPreference(key, value)
                return
            }

            updatePreference(key, value)
        }

        fun insertPreference(key: String, value: String) {
            Preferences.insert {
                it[Preferences.key] = key
                it[Preferences.value] = value
            }
        }

        fun updatePreference(key: String, value: String) {
            Preferences.update(where = {
                Preferences.key eq key
            }, body = {
                it[Preferences.value] = value
            })
        }

        fun deletePreference(key: String) {
            Preferences.deleteWhere { Preferences.key eq key }
        }
    }
}
