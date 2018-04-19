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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.meta;

import javax.annotation.Nonnull;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

public class ParameterInfo {

    public static final String TYPE_REQUEST = "<request>";
    public static final String TYPE_RESPONSE = "<response>";
    public static final String TYPE_UPLOAD = "<upload>";
    public static final String TYPE_JSON_BODY = "<json-body>";
    public static final String TYPE_STREAM_BODY = "<stream-body>";

    private final String name;

    private final Field sirenField;

    private final Class<?> type;

    private final int parameterIdx;

    public ParameterInfo(@Nonnull Field sirenField, @Nonnull Class<?> type, int parameterIdx) {
        this.name = sirenField.getName();
        this.sirenField = sirenField;
        this.type = type;
        this.parameterIdx = parameterIdx;
    }

    public ParameterInfo(@Nonnull String name, int parameterIdx) {
        this.name = name;
        this.sirenField = null;
        this.type = null;
        this.parameterIdx = parameterIdx;
    }

    public String getName() {
        return name;
    }

    public Field getSirenField() {
        return sirenField;
    }

    public Class<?> getType() {
        return type;
    }

    public int getIdx() {
        return parameterIdx;
    }
}
