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
 * Defined a property of the model.  When a model is serialized the property is included in
 * the properties section of the serialized JSON.
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiProperty {

    /**
     * Pseudo class that is active for context calculation if the entity is serialized as sub entity.
     */
    String CONTEXT_SUB_ENTITY = "context-subentity";

    /**
     * Pseudo class that is active for context calculation if the entity is serialized top level.
     */
    String CONTEXT_ENTITY = "context-entity";

    /**
     * Name of the property. If both, {@link #name()} and value are give, the result is undefined.
     * @return the name
     */
    String value() default "";

    /**
     * Name of the property.
     * @return the name
     */
    String name() default "";

    /**
     * Defines the context where a property should be included. If none of the values matches the classes of the
     * containing entity, then the property should not be serialized. The empty set matches any class.
     * @return the set of classes where this property is active.
     */
    String[] context() default {};

    /**
     * Applies only to map properties such as for each element of the map a property should be serialized. if {@code false},
     * then the map itself should be serialized as 1 property (eg. JSON Object).
     * @return {@code true} to flatten a map property
     */
    boolean flatten() default false;
}
