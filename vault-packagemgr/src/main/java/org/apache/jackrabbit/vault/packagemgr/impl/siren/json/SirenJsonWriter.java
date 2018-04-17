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
package org.apache.jackrabbit.vault.packagemgr.impl.siren.json;

import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.vault.fs.api.FilterSet;
import org.apache.jackrabbit.vault.fs.api.PathFilter;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.filter.DefaultPathFilter;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

/**
 * {@code SirenJsonWriter}...
 */
public class SirenJsonWriter implements AutoCloseable {

    private JsonGenerator w;

    public SirenJsonWriter(JsonGenerator writer) {
        this.w = writer;
    }

    public SirenJsonWriter(Writer writer) {
        this(writer, false);
    }

    public SirenJsonWriter(Writer writer, boolean prettyPrint) {
        this(Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, prettyPrint)).createGenerator(writer));
    }

    public void write(@Nonnull Entity e) throws JsonException {
        write(e, false);
    }

    private void write(@Nonnull Entity e, boolean isSubEntity) throws JsonException {
        w.writeStartObject();
        write("class", e.getClasses());
        if (isSubEntity) {
            write("rel", e.getRels());
            writeIfNotEmpty("href", e.getHref());
        }
        write("properties", e.getProperties());
        writeLinks(e.getLinks());
        writeEntities(e.getEntities());
        writeActions(e.getActions());
        w.writeEnd();
    }

    public void close() {
        w.close();
    }

    private void writeValue(@Nonnull Object value) {
        if (value.getClass().isArray()) {
            w.writeStartArray();
            for (Object o : (Object[]) value) {
                writeValue(o);
            }
            w.writeEnd();
        } else if (value instanceof Integer) {
            w.write((Integer) value);
        } else if (value instanceof Long) {
            w.write((Long) value);
        } else if (value instanceof Float) {
            w.write((Float) value);
        } else if (value instanceof Double) {
            w.write((Double) value);
        } else if (value instanceof Boolean) {
            w.write((Boolean) value);
        } else if (value instanceof Calendar) {
            w.write(ISO8601.format((Calendar) value));
        } else if (value instanceof Map) {
            //noinspection unchecked
            write(null, (Map<String, Object>) value);
        } else {
            w.write(value.toString());
        }
    }


    private void writeActions(@Nullable Collection<Action> actions) throws JsonException {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        w.writeStartArray("actions");
        for (Action a: actions) {
            w.writeStartObject();
            write("method", a.getMethod());
            writeIfNotEmpty("name", a.getName());
            writeIfNotEmpty("type", a.getType());
            writeIfNotEmpty("title", a.getTitle());
            writeIfNotEmpty("href", a.getHref());
            writeFields(a.getFields());
            w.writeEnd();
        }
        w.writeEnd();
    }

    private void writeFields(@Nullable Collection<Field> fields) throws JsonException {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        w.writeStartArray("fields");
        for (Field f: fields) {
            w.writeStartObject();
            writeIfNotEmpty("name", f.getName());
            writeIfNotEmpty("type", f.getType());
            writeIfNotEmpty("title", f.getTitle());
            writeIfNotEmpty("value", f.getValue());
            w.writeEnd();
        }
        w.writeEnd();
    }

    private void writeEntities(@Nullable Iterable<Entity> entities) throws JsonException {
        if (entities == null) {
            return;
        }
        boolean hasEntities = false;
        for (Entity sub: entities) {
            if (!hasEntities) {
                w.writeStartArray("entities");
                hasEntities = true;
            }
            write(sub, true);
        }
        if (hasEntities) {
            w.writeEnd();
        }
    }

    private void writeLinks(@Nullable Collection<Link> links) throws JsonException {
        if (links == null || links.isEmpty()) {
            return;
        }
        w.writeStartArray("links");
        for (Link link : links) {
            w.writeStartObject();
            write("class", link.getClasses());
            write("rel", link.getRels());
            writeIfNotEmpty("href", link.getHref());
            writeIfNotEmpty("title", link.getTitle());
            writeIfNotEmpty("type", link.getType());
            w.writeEnd();
        }
        w.writeEnd();

    }
    private void write(@Nullable String key, @Nullable Map<String, Object> props) throws JsonException {
        if (props == null || props.isEmpty()) {
            return;
        }
        if (key == null) {
            w.writeStartObject();
        } else {
            w.writeStartObject(key);
        }
        for (Map.Entry<String, Object> e: props.entrySet()) {
            Object v = e.getValue();
            if (v.getClass().isArray()) {
                w.writeStartArray(e.getKey());
                int len = Array.getLength(v);
                for (int i=0; i<len; i++) {
                    writeValue(Array.get(v, i));
                }
                w.writeEnd();
            } else if (v instanceof WorkspaceFilter) {
                write(e.getKey(), (WorkspaceFilter) v);
            } else if (v instanceof AccessControlHandling) {
                w.write(e.getKey(), ((AccessControlHandling) v).name().toLowerCase());
            } else if (v instanceof Integer) {
                w.write(e.getKey(), (Integer) v);
            } else if (v instanceof Long) {
                w.write(e.getKey(), (Long) v);
            } else if (v instanceof Float) {
                w.write(e.getKey(), (Float) v);
            } else if (v instanceof Double) {
                w.write(e.getKey(), (Double) v);
            } else if (v instanceof Boolean) {
                w.write(e.getKey(), (Boolean) v);
            } else if (v instanceof Calendar) {
                w.write(e.getKey(), ISO8601.format((Calendar) v));
            } else if (v instanceof Map) {
                //noinspection unchecked
                write(e.getKey(), (Map<String, Object>) v);
            } else {
                w.write(e.getKey(), v.toString());
            }
        }
        w.writeEnd();
    }

    private void write(@Nonnull String key, @Nullable WorkspaceFilter filter) throws JsonException {
        if (filter != null) {
            w.writeStartObject(key);
            w.writeStartArray("filters");
            for (PathFilterSet set : filter.getFilterSets()) {
                w.writeStartObject();
                w.write("root", set.getRoot());
                w.write("mode", set.getImportMode().name().toLowerCase());
                Boolean defaultAllow = null;
                for (FilterSet.Entry<PathFilter> e: set.getEntries()) {
                    if (defaultAllow == null) {
                        w.writeStartArray("rules");
                        defaultAllow = !e.isInclude();
                    }
                    w.writeStartObject();
                    w.write("type", e.isInclude() ? "include" : "exclude");
                    PathFilter f = e.getFilter();
                    String pattern;
                    if (f instanceof DefaultPathFilter) {
                        pattern = ((DefaultPathFilter) f).getPattern();
                    } else {
                        pattern = e.toString();
                    }
                    w.write("pattern", pattern);
                    w.writeEnd();
                }
                if (defaultAllow == null) {
                    defaultAllow = true;
                } else {
                    w.writeEnd();
                }
                w.write("default", defaultAllow ? "include" : "exclude");
                w.writeEnd();
            }
            w.writeEnd();
            w.writeEnd();
        }
    }

    private void write(@Nonnull String key, @Nullable  Set<String> strings) throws JsonException {
        if (strings != null && !strings.isEmpty()) {
            w.writeStartArray(key);
            for (String s: strings) {
                w.write(s);
            }
            w.writeEnd();
        }
    }

    private void write(@Nonnull String key, @Nullable Action.Method method) {
        if (method != null) {
            w.write(key, method.name());
        }
    }

    private void writeIfNotNull(@Nonnull String key, @Nullable Object value) throws JsonException {
        if (value != null) {
            w.write(key, value.toString());
        }
    }

    private void writeIfNotEmpty(@Nonnull String key, @Nullable String value) throws JsonException {
        if (value != null && value.length() > 0) {
            w.write(key, value);
        }
    }

}