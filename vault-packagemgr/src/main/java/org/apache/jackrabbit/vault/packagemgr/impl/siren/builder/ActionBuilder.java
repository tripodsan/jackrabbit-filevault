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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

/**
 * {@code ActionBuilder}...
 */
public class ActionBuilder implements Action {

    private String name;

    private String type;

    private String title;

    private String method = "GET";

    private String href = "";

    private List<Field> fields = new LinkedList<Field>();

    public ActionBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ActionBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public ActionBuilder withHref(String href) {
        this.href = href;
        return this;
    }

    public ActionBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ActionBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionBuilder that = (ActionBuilder) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(title, that.title) &&
                Objects.equals(method, that.method) &&
                Objects.equals(href, that.href) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, title, method, href, fields);
    }
}