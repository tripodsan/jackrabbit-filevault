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

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;

/**
 * {@code FieldBuilder}...
 */
public class FieldBuilder extends BaseBuilder<FieldBuilder> {

    private String value;

    public FieldBuilder() {
        withType(Field.Type.TEXT);
    }

    public FieldBuilder withType(Field.Type type) {
        return this.withType(type.toString());
    }

    public FieldBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public Field build() {
        return new FieldImpl();
    }

    private class FieldImpl implements Field {

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

        public String getValue() {
            return value;
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
            FieldImpl that = (FieldImpl) o;
            return Objects.equals(name, that.getName()) &&
                    Objects.equals(type, that.getType()) &&
                    Objects.equals(title, that.getTitle()) &&
                    Objects.equals(value, that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, title, value);
        }

    }
}