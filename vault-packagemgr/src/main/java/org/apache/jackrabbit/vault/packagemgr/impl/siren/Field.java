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

/**
 * {@code Field}...
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

    String getName();

    String getType();

    String getTitle();

    String getValue();

}