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

import com.github.vatbub.finance.manager.RunnableWithProgressUpdates
import com.github.vatbub.finance.manager.util.allEqual

private object Cache {
    val cachedSimilarities = mutableMapOf<BankTransaction, Map<BankTransaction, Double>>()
}

fun List<BankTransaction>.findRecurringTransactions(
    suggestionThreshold: Double = 0.85,
    useCachedSimilarities: Boolean = false
) =
    RunnableWithProgressUpdates<List<List<BankTransaction>>>(
        taskMessage = "Searching for patterns in your transactions...",
        totalSteps = this.size.toLong()
    ) {
        val similarities =
            this@findRecurringTransactions
                .associateWith { transaction ->
                    stepDone()

                    val cachedValue = Cache.cachedSimilarities[transaction]
                    if (useCachedSimilarities && cachedValue != null) return@associateWith cachedValue

                    this@findRecurringTransactions.calculateSimilarities(transaction)
                        .also { Cache.cachedSimilarities[transaction] = it }
                }
        val filteredSimilarities = similarities
            .map {
                it.key to it.value.filterValues { similarity -> similarity >= suggestionThreshold }
            }
            .filter { it.second.size > 1 }
            .filterNot {
                it.second.keys
                    .mapNotNull { transaction -> transaction.recurringBankTransaction.value }
                    .allEqual()
            }
            .toMap()


        val similaritiesWithoutDuplicates = mutableListOf<Pair<BankTransaction, Map<BankTransaction, Double>>>()

        filteredSimilarities.forEach { (transaction, similarityMap) ->
            if (similaritiesWithoutDuplicates
                    .map { it.second }
                    .any { entry -> entry.keys.contains(transaction) }
            )
                return@forEach

            similaritiesWithoutDuplicates.add(transaction to similarityMap)
        }

        return@RunnableWithProgressUpdates similaritiesWithoutDuplicates
            .sortedByDescending { it.second.values.average() }
            .map {
                if (it.second.keys.contains(it.first))
                    it.second.keys.toList()
                else
                    (it.second.keys + it.first).toList()
            }
    }

private fun List<BankTransaction>.calculateSimilarities(transaction: BankTransaction) =
    this.filter { otherTransaction -> transaction != otherTransaction }
        .associateWith { otherTransaction -> transaction similarityTo otherTransaction }
