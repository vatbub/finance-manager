package com.github.vatbub.finance.manager.model

data class Account(
    val name: String,
    val transactions: List<BankTransaction>,
    val id: Int? = null
){
    val balance = transactions.sumOf { it.amount.amount }
}
