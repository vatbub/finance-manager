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
import com.github.vatbub.finance.manager.database.ListChange.OtherChange
import com.github.vatbub.finance.manager.database.ListChange.RemovalChange
import com.github.vatbub.finance.manager.database.Tags.tag
import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.BankTransaction
import com.github.vatbub.finance.manager.util.toContinuousSegments
import com.github.vatbub.kotlin.preferences.KeyValueProvider
import com.github.vatbub.kotlin.preferences.MemoryKeyValueProvider
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
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

    private fun initializeScheme() {
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

    object DatabaseContents : ObservableList<Account> {
        private object Lock

        private fun <T> lock(block: (Transaction) -> T) = synchronized(Lock) {
            transaction {
                block(this)
            }
        }

        private var actualCache: List<Account>? = null
        private val cache: List<Account>
            get() {
                actualCache?.let { return it }
                synchronized(Lock) {
                    actualCache?.let { return it }
                    actualCache = transaction {
                        Internal.getAllAccounts()
                    }
                    return actualCache!!
                }
            }

        private fun invalidateCache() = synchronized(Lock) {
            actualCache = null
        }

        private val changeListeners = mutableListOf<ListChangeListener<in Account>>()

        override fun addListener(listener: ListChangeListener<in Account>) {
            changeListeners.add(listener)
        }

        override fun removeListener(listener: ListChangeListener<in Account>) {
            changeListeners.remove(listener)
        }

        override fun addListener(listener: InvalidationListener?) {
        }

        override fun removeListener(listener: InvalidationListener?) {
        }

        private fun fireChange(block: () -> List<ListChange<Account>>) {
            if (changeListeners.isEmpty()) return
            val change = ListChangeListenerImplementation<Account>(this, block())
            changeListeners.forEach { it.onChanged(change) }
        }

        override val size: Int
            get() = cache.size

        override fun containsAll(elements: Collection<Account>): Boolean =
            cache.containsAll(elements)

        override fun indexOf(element: Account?): Int = cache.indexOf(element)

        override fun contains(element: Account?): Boolean = cache.contains(element)

        override fun get(index: Int): Account = cache[index]

        override fun isEmpty(): Boolean = cache.isEmpty()

        override fun lastIndexOf(element: Account?): Int = cache.lastIndexOf(element)

        override fun add(element: Account): Boolean = lock {
            val createdAccount = Internal.createAccount(element)
            invalidateCache()
            fireChange {
                val index = cache.indexOf(createdAccount)
                listOf(OtherChange(index, index))
            }
            true
        }

        override fun add(index: Int, element: Account?) {
            throw NotSupportedException("The database does not support adding accounts with a specified index. Please use add(element) instead")
        }

        override fun addAll(vararg elements: Account): Boolean = addAll(elements.toList())

        override fun iterator(): MutableIterator<Account> {
            TODO("Not yet implemented")
        }

        override fun addAll(index: Int, elements: Collection<Account>): Boolean {
            throw NotSupportedException("The database does not support adding accounts with a specified index. Please use addAll(elements) instead")
        }

        override fun addAll(elements: Collection<Account>): Boolean = lock {
            val createdAccounts = elements.map { Internal.createAccount(it) }
            invalidateCache()
            fireChange {
                val indices = createdAccounts.map { cache.indexOf(it) }
                listOf(OtherChange(indices.minOrNull() ?: 0, indices.maxOrNull() ?: 0))
            }
            true
        }

        override fun clear() = lock {
            Internal.clearAccounts()
            invalidateCache()
        }

        override fun remove(from: Int, to: Int) = lock {
            val deletedAccounts = (from..to).map { cache[it] }
            deletedAccounts.forEach { Internal.deleteAccount(it) }
            invalidateCache()
            fireChange { listOf(RemovalChange(from, to, deletedAccounts)) }
        }

        override fun remove(element: Account): Boolean = lock {
            val index = cache.indexOf(element)
            if (index < 0) return@lock false

            Internal.deleteAccount(element)
            invalidateCache()
            fireChange { listOf(RemovalChange(index, index, listOf(element))) }
            return@lock true
        }

        override fun removeAll(vararg elements: Account): Boolean = removeAll(elements.toList())

        override fun removeAll(elements: Collection<Account>): Boolean = lock {
            val indexedElements = elements
                .associateBy { cache.indexOf(it) }
                .filter { it.key >= 0 }
            if (indexedElements.isEmpty()) return@lock false

            elements.forEach { Internal.deleteAccount(it) }
            invalidateCache()
            fireChange {
                indexedElements.toContinuousSegments()
                    .map { segment ->
                        RemovalChange(segment.keys.minOrNull()!!, segment.keys.maxOrNull()!!, segment.values.toList())
                    }
            }

            return@lock true
        }

        override fun removeAt(index: Int): Account  = lock {
            val element = cache[index]
            Internal.deleteAccount(element)
            invalidateCache()
            fireChange { listOf(RemovalChange(index, index, listOf(element))) }
            return@lock element
        }

        override fun retainAll(vararg elements: Account): Boolean = retainAll(elements.toList())

        override fun retainAll(elements: Collection<Account>): Boolean = synchronized(Lock) {
            removeAll(cache.filterNot { elements.contains(it) })
        }

        override fun set(index: Int, element: Account?): Account {
            throw NotSupportedException("The database does not support setting accounts with a specified index. Please remove the account and add a new one instead")
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Account> {
            TODO("Not yet implemented")
        }

        override fun setAll(vararg elements: Account?): Boolean {
            TODO("Not yet implemented")
        }

        override fun setAll(col: MutableCollection<out Account>?): Boolean {
            TODO("Not yet implemented")
        }

        override fun listIterator(): MutableListIterator<Account> {
            TODO("Not yet implemented")
        }

        override fun listIterator(index: Int): MutableListIterator<Account> {
            TODO("Not yet implemented")
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

        fun clearAccounts() {
            BankTransactionsToTagsRelation.deleteAll()
            BankTransactions.deleteAll()
            Accounts.deleteAll()
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

        fun updateAccount(accountId: Int, newName: String) {
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

private fun <V>Map<Int, V>.toContinuousSegments():List<Map<Int, V>> = this
    .keys
    .toList()
    .toContinuousSegments()
    .map { segment ->
        segment.associateWith { this[it]!! }
    }
