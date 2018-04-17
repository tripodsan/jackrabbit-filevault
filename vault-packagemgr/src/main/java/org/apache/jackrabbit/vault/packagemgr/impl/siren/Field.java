/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License
* Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.vault.packagemgr.impl.siren;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fields represent controls inside of actions.
 */
public interface Field {

    enum Type {
        HIDDEN,
        TEXT,
        SEARCH,
        TEL,
        URL,
        EMAIL,
        PASSWORD,
        DATETIME,
        DATETIME_LOCAL,
        DATE,
        MONTH,
        WEEK,
        TIME,
        NUMBER,
        RANGE,
        COLOR,
        CHECKBOX,
        RADIO,
        FILE;


        @Override
        public String toString() {
            if (this == DATETIME_LOCAL) {
                return "datetime-local";
            }
            return super.toString().toLowerCase();
        }
    }

    /**
     * A name describing the control. Field names MUST be unique within the set of fields for an action.
     * The behaviour of clients when parsing a Siren document that violates this constraint is undefined. Required.
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Describes aspects of the field based on the current representation. Possible values are implementation-dependent
     * and should be documented. Optional.
     * @return the classes
     */
    @Nullable
    Set<String> getClasses();

    /**
     * The input type of the field. This may include any of the following input types specified in HTML5. see {@link Field.Type}.
     * When missing, the default value is text. Serialization of these fields will depend on the value of the action's type attribute. Optional.
     * @return the type
     */
    @Nullable
    String getType();

    /**
     * Textual annotation of a field. Clients may use this as a label. Optional.
     * @return the title
     */
    @Nullable
    String getTitle();

    /**
     * A default value assigned to the field. Optional.
     * @return the value
     */
    @Nullable
    String getValue();

}