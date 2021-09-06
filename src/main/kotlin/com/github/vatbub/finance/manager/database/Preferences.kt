package com.github.vatbub.finance.manager.database

import com.github.vatbub.finance.manager.view.AccountDisplayTimeUnit
import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.KeyValueProvider
import com.github.vatbub.kotlin.preferences.MemoryKeyValueProvider
import com.github.vatbub.kotlin.preferences.Preferences

val preferences: Preferences
    get() = Preferences(PreferenceHelper.currentProvider)

private object PreferenceHelper {
    val memoryKeyValueProvider = MemoryKeyValueProvider()
    var currentProvider: KeyValueProvider = memoryKeyValueProvider

    init {
        MemoryDataHolder.currentInstance.addListener { _, oldHolder, newHolder ->
            if (newHolder == null) {
                currentProvider = memoryKeyValueProvider
                return@addListener
            }

            currentProvider = newHolder

            if (oldHolder == null) flushPendingPreferenceOperations(newHolder)
        }
    }

    private fun flushPendingPreferenceOperations(newProvider: KeyValueProvider) {
        val copyOfMemoryContents = memoryKeyValueProvider.contents.toList()

        copyOfMemoryContents.forEach { pair ->
            newProvider[pair.first] = pair.second
        }
    }
}

object PreferenceKeys {
    object MainView {
        object LastTimeWindowUnit : Key<AccountDisplayTimeUnit>(
            "MainView.lastTimeWindowUnit",
            AccountDisplayTimeUnit.Months,
            { AccountDisplayTimeUnit.valueOf(it) },
            { it.toString() })

        object LastTimeWindowAmount : Key<Long>("MainView.lastTimeWindowAmount", 1, { it.toLong() }, { it.toString() })
    }
}
