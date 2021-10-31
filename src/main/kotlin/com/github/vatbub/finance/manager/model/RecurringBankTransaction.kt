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

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue

class RecurringBankTransaction(val description: StringProperty) : ObservableWithObservableProperties {
    constructor(description: String) : this(SimpleStringProperty(description))

    override val observableProperties: List<ObservableValue<*>> = listOf(
        description
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecurringBankTransaction) return false

        if (description.value != other.description.value) return false

        return true
    }

    override fun hashCode(): Int {
        return description.hashCode()
    }
}
