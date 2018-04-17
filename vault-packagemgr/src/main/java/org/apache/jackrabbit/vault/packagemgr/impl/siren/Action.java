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
package org.apache.jackrabbit.vault.packagemgr.impl.siren;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Actions show available behaviors an entity exposes.
 */
public interface Action extends Comparable<Action> {

    String TYPE_MULTIPART_FORM_DATA = "multipart/form-data";

    String TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    String TYPE_JSON = "application/json";

    String TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    enum Method {
        POST,
        PUT,
        DELETE,
        PATCH,
        GET
    }

    /**
     * A string that identifies the action to be performed. Action names MUST be unique within the set of actions for an
     * entity. The behaviour of clients when parsing a Siren document that violates this constraint is undefined. Required.
     * @return the name of this action
     */
    @Nonnull
    String getName();

    /**
     * Describes the nature of an action based on the current representation. Possible values are implementation-dependent
     * and should be documented. Optional.
     * @return the classes
     */
    @Nullable
    Set<String> getClasses();

    /**
     * An enumerated attribute mapping to a protocol method. For HTTP, these values may be GET, PUT, POST, DELETE, or PATCH.
     * If this attribute is omitted, GET should be assumed. Optional.
     * @return the action method
     */
    @Nullable
    Method getMethod();

    /**
     * The URI of the action. Required.
     * @return the uri of the action
     */
    @Nonnull
    String getHref();

    /**
     * The encoding type for the request. When omitted and the fields attribute exists, the default value is
     * {@value #TYPE_X_WWW_FORM_URLENCODED}. Optional.
     * @return the type.
     */
    @Nullable
    String getType();

    /**
     * Descriptive text about the action. Optional.
     * @return the title
     */
    @Nullable
    String getTitle();

    /**
     * A collection of fields. Optional.
     * @return the fields
     */
    @Nullable
    Collection<Field> getFields();
}