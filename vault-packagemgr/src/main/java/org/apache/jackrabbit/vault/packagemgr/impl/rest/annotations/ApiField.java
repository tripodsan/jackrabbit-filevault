/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a field of an action of an entity.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiField {

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

    String value() default "";

    String name() default "";

    Type type() default Type.TEXT;

    String title() default "";

    String defaultValue() default "";

}
