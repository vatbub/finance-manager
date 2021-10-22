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

import com.github.vatbub.finance.manager.logging.logger
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.concurrent.Worker.State.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object BackgroundScheduler {
    val singleThreaded = SingleThreadedBackgroundScheduler()
    val multiThreaded = MultiThreadedBackgroundScheduler()
    val onTaskEnqueuedListeners = mutableListOf<(Task<*>) -> Unit>(this::addToTaskQueue)

    val taskQueue: ObservableList<Task<*>> = FXCollections.observableArrayList()

    private fun addToTaskQueue(task: Task<*>) {
        taskQueue.add(task)
        task.stateProperty().addListener { _, _, newState ->
            if (taskFinishedStates.contains(newState))
                taskQueue.remove(task)
        }
    }

    fun shutdown(onShutdownCompleteCallback: (() -> Unit)? = null) {
        singleThreaded.shutdown {
            multiThreaded.shutdown(onShutdownCompleteCallback)
        }
    }

    fun shutdownAndWait() {
        singleThreaded.shutdownAndWait()
        multiThreaded.shutdownAndWait()
    }

    val taskEnqueuedStates = listOf(READY, SCHEDULED)
    val taskRunningStates = listOf(RUNNING)
    val taskFinishedStates = listOf(FAILED, CANCELLED, SUCCEEDED)
}


abstract class BackgroundSchedulerBase {
    val isShuttingDown: Boolean
        get() = executorService.isShutdown

    protected abstract val executorService: ExecutorService

    fun enqueue(task: Task<*>) {
        BackgroundScheduler.onTaskEnqueuedListeners.forEach { it.invoke(task) }
        executorService.submit(task)
    }

    fun enqueue(message: String? = null, runnable: Runnable) = enqueue(runnable.toTask(message))

    fun <T> enqueue(message: String? = null, callable: Callable<T>) = enqueue(callable.toTask(message))

    fun shutdown(onShutdownCompleteCallback: (() -> Unit)? = null) {
        executorService.shutdown()
        if (onShutdownCompleteCallback == null) return
        Thread {
            while (!executorService.isTerminated) {
                Thread.sleep(100)
            }
            onShutdownCompleteCallback()
        }.also {
            it.start()
        }
    }

    fun shutdownAndWait() {
        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(100)
        }
    }

    private fun Runnable.toTask(message: String?) = object : Task<Unit>() {
        init {
            if (message != null) updateMessage(message)
        }

        override fun failed() {
            exception?.let { throw it }
        }

        override fun call() {
            if (message != null) logger.info(message)
            else logger.info("Starting a background task...")
            this@toTask.run()
        }
    }

    private fun <T> Callable<T>.toTask(message: String?) = object : Task<T>() {
        init {
            if (message != null) updateMessage(message)
        }

        override fun failed() {
            exception?.let { throw it }
        }

        override fun call(): T {
            if (message != null) logger.info(message)
            else logger.info("Starting a background task...")
            return this@toTask.call()
        }
    }
}

class SingleThreadedBackgroundScheduler : BackgroundSchedulerBase() {
    override val executorService: ExecutorService = Executors.newSingleThreadExecutor()
}

class MultiThreadedBackgroundScheduler(maxPoolSize: Int = 5) : BackgroundSchedulerBase() {
    override val executorService: ExecutorService = Executors.newFixedThreadPool(maxPoolSize)
}
