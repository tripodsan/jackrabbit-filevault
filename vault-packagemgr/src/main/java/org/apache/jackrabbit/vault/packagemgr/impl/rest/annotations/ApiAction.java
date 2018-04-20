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
 * Defines a action of an entity.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAction {

    String CONTEXT_HIDDEN = ":context-hidden";

    enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }

    String TYPE_MULTIPART_FORM_DATA = "multipart/form-data";

    String TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    String TYPE_JSON = "application/json";

    String TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    String value() default "";

    String name() default "";

    String type() default "";

    String title() default "";

    String href() default "";

    Method method() default Method.POST;

    /**
     * Defines the context where a action should be included. If none of the values matches the classes of the
     * containing entity, then the action should not be serialized. The empty set matches any class.
     * @return the set of classes where this action is active.
     */
    String[] context() default {};

    ApiField[] fields() default {};

}
