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

import kotlin.math.min

infix fun CharSequence.levenshtein(right: CharSequence): Int {
    if (this == right) return 0
    if (this.isEmpty()) return right.length
    if (right.isEmpty()) return this.length

    val leftLength = this.length + 1
    val rightLength = right.length + 1

    var cost = Array(leftLength) { it }
    var newCost = Array(leftLength) { 0 }

    for (rightIndex in 1 until rightLength) {
        newCost[0] = rightIndex

        for (leftIndex in 1 until leftLength) {
            val match = if (this[leftIndex - 1] == right[rightIndex - 1]) 0 else 1

            val costReplace = cost[leftIndex - 1] + match
            val costInsert = cost[leftIndex] + 1
            val costDelete = newCost[leftIndex - 1] + 1

            newCost[leftIndex] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost.last()
}
