/*-
 * #%L
 * Smart Charge
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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

import com.github.vatbub.finance.manager.view.MainView
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

const val appId = "com.github.vatbub.finance.manager"

class EntryClass private constructor(callLaunch: Boolean, vararg args: String?) : Application() {
    companion object {
        var instance: EntryClass? = null

        fun actualMain(vararg args: String) {
            EntryClass(true, *args)
        }
    }

    init {
        @Suppress("SENSELESS_COMPARISON")
        if (callLaunch)
            launch(*args)
    }

    @Suppress("unused")
    constructor() : this(false, null)

    var currentStage: Stage? = null
        private set
    var controllerInstance: MainView? = null
        private set

    override fun start(primaryStage: Stage) {
        instance = this
        currentStage = primaryStage

        val fxmlLoader = FXMLLoader(javaClass.getResource("view/MainView.fxml"), null)
        val root = fxmlLoader.load<Parent>()
        controllerInstance = fxmlLoader.getController()

        val scene = Scene(root)
        primaryStage.title = "Finance manager"
        // val iconName = "icon.png"
        // primaryStage.icons.add(Image(javaClass.getResourceAsStream(iconName)))

        primaryStage.minWidth = root.minWidth(0.0) + 70
        primaryStage.minHeight = root.minHeight(0.0) + 70

        primaryStage.scene = scene

        primaryStage.show()
    }

    override fun stop() {
        BackgroundScheduler.shutdown()
    }
}
