package com.github.vatbub.finance.manager.database

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

sealed class ListChange<T> {
    abstract val from: Int
    abstract val to: Int

    data class RemovalChange<T>(override val from: Int, override val to: Int, val removed: List<T>) : ListChange<T>()

    data class PermutationChange<T>(val permutations: Map<Int, Int>) : ListChange<T>() {
        override val from: Int = permutations.keys.minOrNull() ?: 0
        override val to: Int = permutations.keys.maxOrNull() ?: 0
    }

    data class OtherChange<T>(override val from: Int, override val to: Int) : ListChange<T>()
}

class ListChangeListenerImplementation<T>(list: ObservableList<T>, private val changes: List<ListChange<T>>) :
    ListChangeListener.Change<T>(list) {
    private var currentIndex = -1

    override fun next(): Boolean {
        currentIndex++
        return changes.size >= currentIndex
    }

    override fun reset() {
        currentIndex = -1
    }

    private val currentChange: ListChange<T>
        get() = changes.getOrNull(currentIndex)
            ?: throw IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.")

    override fun getFrom(): Int = currentChange.from

    override fun getTo(): Int = currentChange.to

    override fun getRemoved(): List<T> = currentChange.let {
        if (it !is ListChange.RemovalChange<T>) listOf()
        else it.removed
    }

    override fun getPermutation(): IntArray = currentChange.let { change ->
        if (change !is ListChange.PermutationChange<T>) intArrayOf()
        else {
            (change.from..change.to)
                .map { index -> change.permutations.getOrDefault(index, index) }
                .toIntArray()
        }
    }
}
