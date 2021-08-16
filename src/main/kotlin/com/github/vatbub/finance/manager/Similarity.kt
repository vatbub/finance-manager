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

import kotlin.math.abs
import kotlin.math.max


fun String?.similarityTo(other: String?): Double {
    if (this == null || other == null) return 0.0
    if (this == other) return 1.0
    val maxLength = max(this.length, other.length).toDouble()
    return (maxLength - (this levenshtein other)) / maxLength
}

fun TransactionCategory?.similarityTo(other: TransactionCategory?): Double {
    if (this == null || other == null) return 0.0
    return if (this == other) 0.0 else 1.0
}

fun List<String>.similarityTo(other: List<String>): Double {
    if (this.isEmpty() && other.isEmpty()) return 1.0
    return List(max(this.size, other.size)) { index ->
        this.getOrNull(index).similarityTo(other.getOrNull(index))
    }.average()
}

fun Double?.similarityTo(other: Double?): Double {
    if (this == null || other == null) return 0.0
    if (this == other) return 1.0
    return (this - other) / max(abs(this), abs(other))
}
