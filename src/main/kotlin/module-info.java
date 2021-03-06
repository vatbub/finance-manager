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
module finance.manager {
    requires javafx.graphics;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires javafx.controls;
    requires org.apache.commons.lang3;
    requires java.logging;
    requires exposed.core;
    requires java.sql;
    requires kotlin.reflect;
    requires kotlinPreferences;
    requires org.slf4j;
    requires org.controlsfx.controls;

    opens com.github.vatbub.finance.manager.view to javafx.graphics, javafx.fxml, javafx.base;
    opens com.github.vatbub.finance.manager to javafx.graphics, javafx.fxml, javafx.base;
    opens com.github.vatbub.finance.manager.model to javafx.graphics, javafx.fxml, javafx.base;
    exports com.github.vatbub.finance.manager.view to kotlin.reflect;
}
