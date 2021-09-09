package com.github.vatbub.finance.manager.database

import com.github.vatbub.finance.manager.model.Account
import com.github.vatbub.finance.manager.model.ObservableWithObservableListProperties
import com.github.vatbub.finance.manager.model.ObservableWithObservableProperties
import com.github.vatbub.kotlin.preferences.KeyValueProvider
import com.github.vatbub.kotlin.preferences.MemoryKeyValueProvider
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

    private val preferenceKeyValueProvider = MemoryKeyValueProvider()

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
        restorePreferences()
        restoreFromDatabase()

        accountList.addListener(listSaveListener)
        accountList.addListenersToObjects()
    }

    private fun privateSave() {
        // TODO Do this asynchronously

        transaction(db = database) {
            tables.forEach { it.deleteAll() }

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


            accountList.forEach { account ->
                val accountId = Accounts.insertAndGetId {
                    it[name] = account.name.value
                }.value

                account.transactions.forEach { bankTransaction ->
                    val bankTransactionId = BankTransactions.insertAndGetId {
                        bankTransaction.toDatabaseBankTransaction(accountId, it)
                    }.value

                    storeTagRelation(
                        bankTransactionId,
                        bankTransaction.tags
                            .map { tag -> tags[tag]!! }
                    )
                }
            }
        }
    }

    private fun storeTagRelation(bankTransactionId: Int, tagIds: List<Int>) {
        tagIds.forEach { tagId ->
            BankTransactionsToTagsRelation.insert {
                it[BankTransactionsToTagsRelation.tagId] = tagId
                it[transactionId] = bankTransactionId
            }
        }
    }

    private fun getAllBankTransactions(accountId: Int) =
        BankTransactions.select {
            BankTransactions.accountId eq accountId
        }.map { it.toMemoryTransaction(getTagsFor(it[BankTransactions.id].value)) }

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
            Accounts.selectAll().map {
                Account(
                    name = it[Accounts.name],
                    transactions = getAllBankTransactions(it[Accounts.id].value)
                )
            }
        }

        accountList.clear()
        accountList.addAll(newAccountList)
    }

    private fun storePreferences() = transaction(db = database) {
        Preferences.deleteAll()
        preferenceKeyValueProvider.contents.forEach { (key, value) ->
            Preferences.insert {
                it[Preferences.key] = key
                it[Preferences.value] = value
            }
        }
    }

    private fun restorePreferences() = transaction(db = database) {
        val preferenceContents = Preferences.selectAll()
            .associate { it[Preferences.key] to it[Preferences.value] }
        preferenceKeyValueProvider.contents.let {
            it.clear()
            it.putAll(preferenceContents)
        }
    }

    override val isPersistent: Boolean = true

    override fun get(key: String): String? = preferenceKeyValueProvider[key]

    override fun set(key: String, value: String?) {
        preferenceKeyValueProvider[key] = value
        storePreferences()
    }
}

private fun File.toJdbcConnectionString() = "jdbc:sqlite:" + this.absolutePath.replace("\\", "/")
private fun String.connectToDatabase() = Database.connect(this, driver = "org.sqlite.JDBC")