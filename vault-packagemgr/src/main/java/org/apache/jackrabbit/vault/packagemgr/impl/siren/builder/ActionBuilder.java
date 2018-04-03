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

import java.util.LinkedList;
import java.util.List;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

/**
 * {@code ActionBuilder}...
 */
public class ActionBuilder extends BaseBuilder implements Action {

    private String method = "GET";

    private String href = "";

    private List<Field> fields = new LinkedList<Field>();

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public ActionBuilder withName(String name) {
        super.withName(name);
        return this;
    }

    public ActionBuilder withHref(String href) {
        this.href = href;
        return this;
    }

    public ActionBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    public ActionBuilder withGET() {
        return withMethod("GET");
    }

    public ActionBuilder withPUT() {
        return withMethod("PUT");
    }

    public ActionBuilder withPOST() {
        return withMethod("POST");
    }

    public ActionBuilder withDELETE() {
        return withMethod("DELETE");
    }

    public ActionBuilder withPATCH() {
        return withMethod("PATCH");
    }

    public ActionBuilder addField(Field field) {
        fields.add(field);
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Iterable<Field> getFields() {
        return fields;
    }
}