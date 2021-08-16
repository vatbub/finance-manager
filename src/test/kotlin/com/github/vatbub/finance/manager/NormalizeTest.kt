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
package com.github.vatbub.finance.manager

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NormalizeTest {
    @Test
    fun allPositiveNumbersAboveOne() {
        val input = listOf(2.0, 3.5, 4.0)
        val expectedOutput = listOf(0.0, 0.75, 1.0)
        assertEquals(expectedOutput, input.normalize())
    }

    @Test
    fun allNegativeNumbers() {
        val input = listOf(-2.0, -3.5, -4.0)
        val expectedOutput = listOf(1.0, 0.25, 0.0)
        assertEquals(expectedOutput, input.normalize())
    }

    private fun <T> assertEquals(expected: List<T>, actual: List<T>) {
        assertEquals(expected.size, actual.size)
        expected.indices.forEach { index ->
            assertEquals(expected[index], actual[index])
        }
    }
}
