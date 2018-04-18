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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

/**
 * {@code ActionBuilder}...
 */
public class ActionBuilder extends BaseBuilder<ActionBuilder> {

    private Action.Method method = Action.Method.GET;

    private List<Field> fields = new LinkedList<>();

    public ActionBuilder withMethod(Action.Method method) {
        this.method = method;
        return this;
    }

    public ActionBuilder addField(Field field) {
        fields.add(field);
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Action build() {
        return new ActionImpl();
    }

    private class ActionImpl implements Action {

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
            if (type == null || type.isEmpty() && fields.size() > 0) {
                return Action.TYPE_X_WWW_FORM_URLENCODED;
            }
            return type;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Collection<Field> getFields() {
            return fields;
        }

        @Nullable
        @Override
        public Set<String> getClasses() {
            return classes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActionImpl that = (ActionImpl) o;
            return Objects.equals(name, that.getName()) &&
                    Objects.equals(type, that.getType()) &&
                    Objects.equals(title, that.getTitle()) &&
                    Objects.equals(method, that.getMethod()) &&
                    Objects.equals(href, that.getHref()) &&
                    Objects.equals(fields, that.getFields());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, title, method, href, fields);
        }

        @Override
        public int compareTo(Action o) {
            int c = name.compareTo(o.getName());
            if (c != 0) {
                return c;
            }
            c = href.compareTo(o.getHref());
            if (c != 0) {
                return c;
            }
            return type.compareTo(o.getType());
        }

    }
}