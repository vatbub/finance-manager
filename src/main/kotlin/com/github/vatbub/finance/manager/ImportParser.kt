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

import com.github.vatbub.finance.manager.model.BankTransaction
import com.github.vatbub.finance.manager.model.Currency.Euro
import com.github.vatbub.finance.manager.model.CurrencyAmount
import com.github.vatbub.finance.manager.model.toTransactionCategory
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

interface ImportParser {
    operator fun invoke(source:File): List<BankTransaction>
}

object ConsorsbankCsvParser : ImportParser {
    override fun invoke(source: File): List<BankTransaction> {
        return source
            .readLines()
            .drop(1)
            .map { line ->
                val parts = line.split(";")
                BankTransaction(
                    parts[0].nullIfEmpty()?.toLocalDate(),
                    parts[1].nullIfEmpty()?.toLocalDate(),
                    parts[2].nullIfEmpty(),
                    parts[3].nullIfEmpty(),
                    parts[4].nullIfEmpty(),
                    parts[5].nullIfEmpty(),
                    parts[6].nullIfEmpty(),
                    parts[7].nullIfEmpty()?.toTransactionCategory(),
                    parts[8].nullIfEmpty()?.let { listOf(it) } ?: listOf(),
                    CurrencyAmount(
                        amount = parts[10].nullIfEmpty()?.replace(".", "")?.replace(",", ".")?.toDouble() ?: 0.00,
                        currency = Euro
                    )
                )
            }
    }
}

private fun String.nullIfEmpty() = this.ifEmpty { null }
private fun String.toLocalDate() = LocalDate.parse(this, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
