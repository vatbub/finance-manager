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

fun List<WeightedValue>.weightedAverage(): Double {
    val weightNormalizationFactor = 1.0 / this.sumOf { it.weight }
    return fold(0.0) { acc, nextValue ->
        acc + nextValue.weight * weightNormalizationFactor * nextValue.value
    }
}

data class WeightedValue(val weight: Double, val value: Double)

infix fun Double.weightOf(value: Double) = WeightedValue(this, value)
