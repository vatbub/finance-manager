package com.github.vatbub.finance.manager.util

fun List<Int>.toContinuousSegments():List<List<Int>> {
    var currentChunk = mutableListOf<Int>()
    val result= mutableListOf<List<Int>>()

    this.sorted().forEach {
        val lastNumber = currentChunk.lastOrNull()
        if (lastNumber == null || it - lastNumber == 1) {
            currentChunk.add(it)
            return@forEach
        }

        result.add(currentChunk)
        currentChunk = mutableListOf()
    }

    return result
}
