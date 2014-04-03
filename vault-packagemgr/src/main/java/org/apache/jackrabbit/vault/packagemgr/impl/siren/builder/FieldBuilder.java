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
package org.apache.jackrabbit.vault.packagemgr.impl.siren.builder;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

/**
 * {@code FieldBuilder}...
 */
public class FieldBuilder implements Field {

    private String name;

    private String stringValue;

    private String type;

    private String title;

    public FieldBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FieldBuilder setStringValue(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }

    public FieldBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public FieldBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getStringValue() {
        return stringValue;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
}