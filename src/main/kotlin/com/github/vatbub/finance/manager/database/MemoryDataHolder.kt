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

import com.github.vatbub.finance.manager.BackgroundScheduler
import com.github.vatbub.finance.manager.RunnableWithProgressUpdates
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.ObservableWithObservableListProperties
import com.github.vatbub.finance.manager.model.ObservableWithObservableProperties
import com.github.vatbub.finance.manager.model.RecurringBankTransaction
import com.github.vatbub.kotlin.preferences.KeyValueProvider
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class MemoryDataHolder(val database: Database) : KeyValueProvider {
    companion object {
        val currentInstance: ObjectProperty<MemoryDataHolder> = SimpleObjectProperty(null)
    }

    constructor(connectionString: String/*, password: String*/) :
            this(connectionString.connectToDatabase())

    constructor(database: File/*, password: String*/) :
            this(database.toJdbcConnectionString())

    val accountList = FXCollections.observableArrayList<Account>()

    private val listSaveListener: ListChangeListener<Any> by lazy {
        ListChangeListener<Any> { change ->
            privateSave()

            change.list.addListenersToObjects()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun propertySaveListener(
        observable: ObservableValue<out Any>?,
        oldValue: Any?,
        newValue: Any?
    ) {
        privateSave()
    }

    private fun List<*>.addListenersToObjects() {
        forEach {
            if (it is ObservableWithObservableProperties) it.addListenerToAllProperties(this@MemoryDataHolder::propertySaveListener)
            if (it is ObservableWithObservableListProperties) it.addListenerToAllLists(listSaveListener)
        }
    }

    init {
        transaction(db = database) { SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray()) }
        restoreFromDatabase()

        accountList.addListener(listSaveListener)
        accountList.addListenersToObjects()
    }

    private fun privateSave() = BackgroundScheduler.singleThreaded.enqueue(RunnableWithProgressUpdates<Unit>(
        taskMessage = "Saving data to disk...",
        totalSteps = financialTables.size.toLong() +
                1L + // Tags
                accountList.size.toLong() +
                accountList.sumOf { it.transactions.size }
    ) {
        transaction(db = database) {
            financialTables.forEach {
                it.deleteAll()
                stepDone()
            }

            val tags = accountList
                .map { account -> account.transactions.map { it.tags } }
                .flatten()
                .flatten()
                .groupBy { it }
                .keys
                .associateWith { tag ->
                    Tags.insertAndGetId {
                        it[Tags.tag] = tag
                    }.value
                }

            stepDone()

            accountList.forEach { account ->
                val accountId = Accounts.insertAndGetId {
                    it[name] = account.name.value
                }.value

                stepDone()

                account.transactions.forEach { bankTransaction ->
                    val recurringTransactionId =
                        bankTransaction.recurringBankTransaction.value?.let { getOrInsertRecurringTransaction(it) }

                    val bankTransactionId = BankTransactions.insertAndGetId {
                        bankTransaction.toDatabaseBankTransaction(accountId, it)
                        it[BankTransactions.recurringTransactionId] = recurringTransactionId
                    }.value

                    storeTagRelation(
                        bankTransactionId,
                        bankTransaction.tags
                            .map { tag -> tags[tag]!! }
                    )

                    stepDone()
                }
            }
        }
    })

    private fun storeTagRelation(bankTransactionId: Int, tagIds: List<Int>) {
        tagIds.forEach { tagId ->
            BankTransactionsToTagsRelation.insert {
                it[BankTransactionsToTagsRelation.tagId] = tagId
                it[transactionId] = bankTransactionId
            }
        }
    }

    private fun getOrInsertRecurringTransaction(recurringBankTransaction: RecurringBankTransaction): Int {
        RecurringTransactions
            .select { RecurringTransactions.description eq recurringBankTransaction.description.value }
            .firstOrNull()
            ?.get(RecurringTransactions.id)
            ?.value
            ?.let { return it }

        return RecurringTransactions.insertAndGetId {
            it[description] = recurringBankTransaction.description.value
        }.value
    }

    private fun getAllBankTransactions(accountId: Int, recurringTransactions: Map<Int, RecurringBankTransaction>) =
        BankTransactions.select {
            BankTransactions.accountId eq accountId
        }.map { it.toMemoryTransaction(getTagsFor(it[BankTransactions.id].value), recurringTransactions) }

    private fun getTagsFor(bankTransactionId: Int): List<String> = transaction {
        val relationQuery = BankTransactionsToTagsRelation.select {
            BankTransactionsToTagsRelation.transactionId eq bankTransactionId
        }
        relationQuery.map { resultRow ->
            Tags.select { Tags.id eq resultRow[BankTransactionsToTagsRelation.tagId] }.first()[Tags.tag]
        }
    }

    private fun restoreFromDatabase() {
        val newAccountList: List<Account> = transaction(db = database) {
            val recurringTransactions = RecurringTransactions
                .selectAll()
                .associate { it[RecurringTransactions.id].value to RecurringBankTransaction(it[RecurringTransactions.description]) }

            Accounts.selectAll().map {
                Account(
                    name = it[Accounts.name],
                    transactions = getAllBankTransactions(it[Accounts.id].value, recurringTransactions)
                )
            }
        }

        accountList.clear()
        accountList.addAll(newAccountList)
    }

    override val isPersistent: Boolean = true

    override fun get(key: String): String? = transaction(db = database) {
        Preferences.select {
            Preferences.key eq key
        }.firstOrNull()
            ?.get(Preferences.value)
    }

    override fun set(key: String, value: String?) = transaction(db = database) {
        if (value == null) {
            Preferences.deleteWhere { Preferences.key eq key }
            return@transaction
        }

        val keyExists = Preferences.select {
            Preferences.key eq key
        }.count() > 0

        if (keyExists) {
            Preferences.insert {
                it[Preferences.key] = key
                it[Preferences.value] = value
            }
        } else {
            Preferences.update(where = {
                Preferences.key eq key
            }) {
                it[Preferences.value] = value
            }
        }
    }
}

private fun File.toJdbcConnectionString() = "jdbc:sqlite:" + this.absolutePath.replace("\\", "/")
private fun String.connectToDatabase() = Database.connect(this, driver = "org.sqlite.JDBC")
