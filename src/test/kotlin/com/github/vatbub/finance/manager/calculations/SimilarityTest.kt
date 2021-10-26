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
package com.github.vatbub.finance.manager.calculations

import com.github.vatbub.finance.manager.model.Currency.Euro
import com.github.vatbub.finance.manager.model.CurrencyAmount
import com.github.vatbub.finance.manager.model.TransactionCategory
import com.github.vatbub.finance.manager.model.TransactionCategory.Education
import com.github.vatbub.finance.manager.model.TransactionCategory.Transfer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class SimilarityTest {
    @Test
    fun equalStringsTest() =
        assertStringSimilarity("Hello world.", "Hello world.", 1.0)

    @Test
    fun anyStringNullTest() =
        assertStringSimilarity("Hello world.", null, 0.0)

    @Test
    fun bothStringNullTest() =
        assertStringSimilarity(null, null, 1.0)

    @Test
    fun everyMutationDecreasesSimilarity() {
        val startString = "Hello there"
        var currentString = startString
        var currentDistance = 1.0

        repeat(startString.length) {
            println(currentString)
            assertStringSimilarity(startString, currentString, currentDistance)
            currentString = currentString.mutate(it)
            currentDistance -= startString.similarityStep
        }
    }

    @Test
    fun everyDeletionDecreasesSimilarity() {
        val startString = "Hello there"
        var currentString = startString
        var currentDistance = 1.0

        repeat(startString.length) {
            println(currentString)
            assertStringSimilarity(startString, currentString, currentDistance)
            currentString = currentString.removeRange(currentString.length - 1, currentString.length)
            currentDistance -= startString.similarityStep
        }
    }

    @Test
    fun everyAdditionDecreasesSimilarity() {
        val startString = ""
        var currentString = startString
        var currentDistance = 1.0

        repeat(startString.length) {
            println(currentString)
            assertStringSimilarity(startString, currentString, currentDistance)
            currentString += random.nextInt().toChar().toString()
            currentDistance -= currentString.similarityStep
        }
    }

    private fun assertStringSimilarity(first: String?, second: String?, expectedSimilarity: Double) {
        Assertions.assertTrue(expectedSimilarity >= 0)
        assertEquals(expectedSimilarity, first.similarityTo(second), 0.0001)
        assertEquals(expectedSimilarity, second.similarityTo(first), 0.0001)
    }

    private val String.similarityStep
        get() = 1.0 / length

    @Test
    fun equalTransactionCategoryTest() {
        assertTransactionCategorySimilarity(Transfer, Transfer, 1.0)
    }

    @Test
    fun differentTransactionCategoryTest() {
        assertTransactionCategorySimilarity(Transfer, Education, 0.0)
    }

    @Test
    fun nullTransactionCategoryTest() {
        assertTransactionCategorySimilarity(null, Transfer, 0.0)
    }

    @Test
    fun bothNullTransactionCategoryTest() {
        assertTransactionCategorySimilarity(null, null, 1.0)
    }

    private fun assertTransactionCategorySimilarity(
        first: TransactionCategory?,
        second: TransactionCategory?,
        expectedSimilarity: Double
    ) {
        Assertions.assertTrue(expectedSimilarity >= 0)
        assertEquals(expectedSimilarity, first.similarityTo(second), 0.0001)
        assertEquals(expectedSimilarity, second.similarityTo(first), 0.0001)
    }

    @Test
    fun equalDoubleTest() {
        assertDoubleSimilarity(10.5, 10.5, 1.0)
    }

    @Test
    fun differentDoublePositiveDirectionTest() {
        val startDouble = 1.5
        repeat(10) {
            assertDoubleSimilarity(startDouble, startDouble + it, 1.0 / (it + 1.0))
        }
    }

    @Test
    fun differentDoubleNegativeDirectionTest() {
        val startDouble = 1.5
        repeat(10) {
            assertDoubleSimilarity(startDouble, startDouble - it, 1.0 / (it + 1.0))
        }
    }

    @Test
    fun nullDoubleTest() {
        assertDoubleSimilarity(null, 10.9, 0.0)
    }

    @Test
    fun bothNullDoubleTest() {
        assertDoubleSimilarity(null, null, 1.0)
    }

    @Test
    fun equalCurrencyAmountTest() {
        assertCurrencyAmountSimilarity(CurrencyAmount(10.5, Euro), CurrencyAmount(10.5, Euro), 1.0)
    }

    @Test
    fun differentCurrencyAmountPositiveDirectionTest() {
        val startValue = CurrencyAmount(1.5, Euro)
        repeat(10) {
            assertCurrencyAmountSimilarity(startValue, startValue + it, 1.0 / (it + 1.0))
        }
    }

    @Test
    fun differentCurrencyAmountNegativeDirectionTest() {
        val startDouble = CurrencyAmount(1.5, Euro)
        repeat(10) {
            assertCurrencyAmountSimilarity(startDouble, startDouble - it, 1.0 / (it + 1.0))
        }
    }

    @Test
    fun nullCurrencyAmountTest() {
        assertCurrencyAmountSimilarity(null, CurrencyAmount(10.9, Euro), 0.0)
    }

    @Test
    fun bothNullCurrencyAmountTest() {
        assertCurrencyAmountSimilarity(null, null, 1.0)
    }

    private operator fun CurrencyAmount.plus(other: Int) = CurrencyAmount(amount + other, currency)
    private operator fun CurrencyAmount.minus(other: Int) = CurrencyAmount(amount - other, currency)

    private fun assertCurrencyAmountSimilarity(
        first: CurrencyAmount?,
        second: CurrencyAmount?,
        expectedSimilarity: Double
    ) =
        assertDoubleSimilarity(first?.amount, second?.amount, expectedSimilarity)

    private fun assertDoubleSimilarity(first: Double?, second: Double?, expectedSimilarity: Double) {
        Assertions.assertTrue(expectedSimilarity >= 0)
        assertEquals(expectedSimilarity, first.similarityTo(second), 0.0001)
        assertEquals(expectedSimilarity, second.similarityTo(first), 0.0001)
    }

    @Test
    fun bothListsEmpty() {
        assertStringListSimilarity(listOf(), listOf(), 1.0)
    }

    @Test
    fun listOfEqualStrings() {
        val list = listOf("Hello there", "Hello world", "Hi, my name is Fred")
        assertStringListSimilarity(list, list, 1.0)
    }

    @Test
    fun listWithOneDifferentEntry() {
        val list1 = listOf("Hello there", "Hello world", "Hi, my name is Fred")
        val list2 = listOf("Hell there", "Hello world", "Hi, my name is Fred")
        assertStringListSimilarity(list1, list2, 0.96969696)
    }

    private fun assertStringListSimilarity(first: List<String>, second: List<String>, expectedSimilarity: Double) {
        Assertions.assertTrue(expectedSimilarity >= 0)
        assertEquals(expectedSimilarity, first.similarityTo(second), 0.0001)
        assertEquals(expectedSimilarity, second.similarityTo(first), 0.0001)
    }

    companion object {
        private val random = Random()
    }
}
