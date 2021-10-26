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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class LevenshteinTest {
    @Test
    fun equalStringsTest() =
        assertLevenshteinDistance("Hello world.", "Hello world.", 0)

    @Test
    fun everyMutationIncreasesLevenshteinDistance() {
        val startString = "Hello there"
        var currentString = startString
        var currentDistance = 0

        repeat(startString.length) {
            println(currentString)
            assertLevenshteinDistance(startString, currentString, currentDistance)
            currentString = currentString.mutate(it)
            currentDistance++
        }
    }

    @Test
    fun everyDeletionIncreasesLevenshteinDistance() {
        val startString = "Hello there"
        var currentString = startString
        var currentDistance = 0

        repeat(startString.length) {
            println(currentString)
            assertLevenshteinDistance(startString, currentString, currentDistance)
            currentString = currentString.removeRange(currentString.length - 1, currentString.length)
            currentDistance++
        }
    }

    @Test
    fun everyAdditionIncreasesLevenshteinDistance() {
        val startString = ""
        var currentString = startString
        var currentDistance = 0

        repeat(startString.length) {
            println(currentString)
            assertLevenshteinDistance(startString, currentString, currentDistance)
            currentString += random.nextInt().toChar().toString()
            currentDistance++
        }
    }

    private fun assertLevenshteinDistance(first: String, second: String, expectedLevenshteinDistance: Int) {
        assertTrue(expectedLevenshteinDistance >= 0)
        assertEquals(expectedLevenshteinDistance, first levenshtein second)
        assertEquals(expectedLevenshteinDistance, second levenshtein first)
    }

    companion object {
        private val random = Random()
    }
}
