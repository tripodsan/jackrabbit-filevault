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
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.apache.jackrabbit.vault.fs.api.FilterSet;
import org.apache.jackrabbit.vault.fs.api.PathFilter;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.filter.DefaultPathFilter;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
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
        this.w = Json.createGenerator(writer);
    }

    public void write(Link link) throws JsonException {
        w.writeStartObject();
        write("rel", link.getRels());
        writeIfNotNull("href", link.getHref());
        writeIfNotEmpty("title", link.getTitle());
        w.writeEnd();
    }

    public void write(Entity e) throws JsonException {
        w.writeStartObject();
        write("class", e.getClasses());
        write("rel", e.getRels());
        writeIfNotEmpty("href", e.getHref());
        if (!e.getProperties().isEmpty()) {
            write("properties", e.getProperties());
        }
        w.writeStartArray("links");
        for (Link link : e.getLinks()) {
            write(link);
        }
        w.writeEnd();
        boolean hasEntities = false;
        for (Entity sub: e.getEntities()) {
            if (!hasEntities) {
                w.writeStartArray("entities");
                hasEntities = true;
            }
            write(sub);
        }
        if (hasEntities) {
            w.writeEnd();
        }

        w.writeEnd();
    }

    public void close() {
        w.close();
    }

    private void write(String key, Map<String, Object> props) throws JsonException {
        w.writeStartObject(key);
        for (Map.Entry<String, Object> e: props.entrySet()) {
            Object v = e.getValue();
            if (v instanceof String[]) {
                w.writeStartArray(e.getKey());
                for (String s : (String[]) v) {
                    w.write(s);
                }
                w.writeEnd();
            } else if (v instanceof WorkspaceFilter) {
                write(e.getKey(), (WorkspaceFilter) v);
            } else {
                w.write(e.getKey(), v.toString());
            }
        }
        w.writeEnd();
    }

    private void write(String key, WorkspaceFilter filter) throws JsonException {
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
    private void write(String key, Set<String> strings) throws JsonException {
        if (!strings.isEmpty()) {
            w.writeStartArray(key);
            for (String s: strings) {
                w.write(s);
            }
            w.writeEnd();
        }
    }

    private void writeIfNotNull(String key, Object value) throws JsonException {
        if (value != null) {
            w.write(key, value.toString());
        }
    }

    private void writeIfNotEmpty(String key, String value) throws JsonException {
        if (value != null && value.length() > 0) {
            w.write(key, value);
        }
    }

}