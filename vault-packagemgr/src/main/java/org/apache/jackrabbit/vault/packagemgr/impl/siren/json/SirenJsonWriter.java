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
import java.util.Properties;
import java.util.Set;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

/**
 * {@code SirenJsonWriter}...
 */
public class SirenJsonWriter {

    private JSONWriter w;

    public SirenJsonWriter(JSONWriter writer) {
        this.w = writer;
    }

    public SirenJsonWriter(Writer writer) {
        this.w = new JSONWriter(writer);
    }

    public void write(Link link) throws JSONException {
        w.object();
        write("rel", link.getRels());
        writeIfNotNull("href", link.getHref());
        writeIfNotEmpty("title", link.getTitle());
        w.endObject();
    }

    public void write(Entity e) throws JSONException {
        w.object();
        write("class", e.getClasses());
        write("rel", e.getRels());
        writeIfNotEmpty("href", e.getHref());
        if (!e.getProperties().isEmpty()) {
            w.key("properties");
            write(e.getProperties());
        }
        w.key("links");
        w.array();
        for (Link link : e.getLinks()) {
            write(link);
        }
        w.endArray();
        boolean hasEntities = false;
        for (Entity sub: e.getEntities()) {
            if (!hasEntities) {
                w.key("entities").array();
                hasEntities = true;
            }
            write(sub);
        }
        if (hasEntities) {
            w.endArray();
        }

        w.endObject();
    }

    public void write(Properties props) throws JSONException {
        w.object();
        for (Map.Entry e: props.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (v instanceof String[]) {
                w.key(key).array();
                for (String s : (String[]) v) {
                    w.value(s);
                }
                w.endArray();
            } else if (v instanceof WorkspaceFilter) {
                write(key, (WorkspaceFilter) v);
            } else {
                w.key(key).value(v);
            }
        }
        w.endObject();
    }

    private void write(String key, WorkspaceFilter filter) throws JSONException {
        if (filter != null) {
            w.key(key);
            w.object();
            w.key("filters").array();
            for (PathFilterSet set : filter.getFilterSets()) {
                w.object();
                w.key("root").value(set.getRoot());
                w.endObject();
            }
            w.endArray();
            w.endObject();
        }
    }
    private void write(String key, Set<String> strings) throws JSONException {
        if (!strings.isEmpty()) {
            w.key(key);
            w.array();
            for (String s: strings) {
                w.value(s);
            }
            w.endArray();
        }
    }

    private void writeIfNotNull(String key, Object value) throws JSONException {
        if (value != null) {
            w.key(key).value(value);
        }
    }

    private void writeIfNotEmpty(String key, String value) throws JSONException {
        if (value != null && value.length() > 0) {
            w.key(key).value(value);
        }
    }

}