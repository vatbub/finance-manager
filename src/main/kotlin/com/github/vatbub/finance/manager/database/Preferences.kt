package com.github.vatbub.finance.manager.database

import com.github.vatbub.finance.manager.view.AccountDisplayTimeUnit
import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.Preferences

val preferences = Preferences(DatabaseManager)

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
