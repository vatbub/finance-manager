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

import com.github.vatbub.finance.manager.ConsorsbankCsvParser
import com.github.vatbub.finance.manager.model.Currency.Euro
import com.github.vatbub.finance.manager.model.TransactionCategory.Transfer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class BankTransactionTest {
    @Test
    fun equalTransactionSimilarity() {
        val transaction = BankTransaction(
            LocalDate.of(2021, 10, 29),
            LocalDate.of(2021, 10, 29),
            "Das Känguru",
            "DE1234567890",
            "CSD123456",
            "Ach dein mein,",
            "das sind doch bürgerliche Kategorien",
            Transfer,
            listOf("Niemand", "hat", "die", "Absicht", "ein", "Bankkonto", "zu", "errichten"),
            CurrencyAmount(-12.0, Euro)
        )

        assertSimilarity(transaction, transaction, 1.0)
    }

    @Test
    fun equalTransactionSimilarity2() {
        val transaction = BankTransaction(
            null,
            null,
            null,
            null,
            null,
            "VISA Kartenzahlung",
            "Vorgemerkter Umsatz: VISA Kartenzahlung",
            null,
            listOf(),
            CurrencyAmount(-3.01, Euro)
        )

        assertSimilarity(transaction, transaction, 1.0)
    }

    @Test
    fun equalTransactionsFromCsv() {
        ConsorsbankCsvParser(sampleCsv)
            .forEach { assertSimilarity(it, it, 1.0) }
    }

    @Suppress("SameParameterValue")
    private fun assertSimilarity(first: BankTransaction, second: BankTransaction, expectedSimilarity: Double) {
        assertTrue(expectedSimilarity >= 0.0)
        assertEquals(expectedSimilarity, first similarityTo second, 0.0001)
        assertEquals(expectedSimilarity, second similarityTo first, 0.0001)
    }

    companion object {
        val sampleCsv = BankTransactionTest::class.java
            .getResource("Umsatzübersicht_210808731.csv")!!
            .toURI()
            .let { File(it) }
    }
}
