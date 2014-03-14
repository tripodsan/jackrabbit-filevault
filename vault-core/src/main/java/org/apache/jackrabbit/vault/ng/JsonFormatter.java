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
package org.apache.jackrabbit.vault.ng;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.fs.impl.io.AggregateWalkListener;

import org.apache.jackrabbit.vault.fs.api.NodeTypesCollector;
import org.apache.jackrabbit.vault.util.ItemNameComparator;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

/**
 * <code>JsonFormatter</code>...
 */
public class JsonFormatter implements TreeWalkerListener {

    /**
     * the export context
     */
    private final NodeTypesCollector ntCollector;

    private final JSONWriter writer;

    private final Writer out;

    // used to temporarily store properties of a node
    private final List<Property> props = new ArrayList<Property>();

    private Set<String> ignored = new HashSet<String>();

    public JsonFormatter(NodeTypesCollector aggregate, OutputStream out)
            throws IOException, RepositoryException {
        this.ntCollector = aggregate;
        this.out = new OutputStreamWriter(out, "utf-8");
        writer = new JSONWriter(this.out);
        writer.setTidy(true);
    }

    public void onWalkBegin(Node root) throws RepositoryException {
        // init ignored protected properties
        ignored.clear();
        ignored.add(JcrConstants.JCR_CREATED);
        ignored.add(JcrConstants.JCR_CREATED_BY);
        ignored.add(JcrConstants.JCR_BASEVERSION);
        ignored.add(JcrConstants.JCR_VERSIONHISTORY);
        ignored.add(JcrConstants.JCR_PREDECESSORS);

    }

    public void onNodeBegin(Node node, boolean included, int depth) throws RepositoryException {
        if (ntCollector != null) {
            // register used node types
            ntCollector.addNodeTypes(node);
        }
        props.clear();

        try {
            if (depth > 0) {
                String label = Text.getName(node.getPath());
                writer.key(label);
            }
            writer.object();
        } catch (JSONException e) {
            throw new RepositoryException(e);
        }
    }

    public void onProperty(Property prop, int depth) throws RepositoryException {
        if (ignored.contains(prop.getName()) && prop.getDefinition().isProtected()) {
            return;
        }
        props.add(prop);
    }

    public void onChildren(Node node, int depth) throws RepositoryException {
        try {
            // attributes (properties)
            Collections.sort(props, ItemNameComparator.INSTANCE);
            for (Property prop: props) {
                // attribute name (encode property name to make sure it's a valid xml name)
                boolean sort = prop.getName().equals(JcrConstants.JCR_MIXINTYPES);
                new JsonProperty(prop).write(writer, sort);
            }

        } catch (JSONException e) {
            throw new RepositoryException(e);
        }
    }

    public void onNodeEnd(Node node, boolean included, int depth) throws RepositoryException {
        onNodeEnd(node, included, depth, null);
    }

    public void onNodeEnd(Node node, boolean included, int depth, Iterable<String> childNodeNames) throws RepositoryException {
        try {
            if (childNodeNames != null) {
                writer.key(":childNames").array();
                for (String name: childNodeNames) {
                    writer.value(name);
                }
                writer.endArray();
            }
            writer.endObject();
        } catch (JSONException e) {
            throw new RepositoryException(e);
        }

    }

    public void onNodeIgnored(Node node, int depth) throws RepositoryException {
        try {
            if (depth == 0) {
                String label = Text.getName(node.getPath());
                writer.key(label).object().endObject();
            }
        } catch (JSONException e) {
            throw new RepositoryException(e);
        }
    }

    public void onWalkEnd(Node root) throws RepositoryException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

}