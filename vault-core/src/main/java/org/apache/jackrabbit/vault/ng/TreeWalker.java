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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.fs.api.ItemFilterSet;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.util.Text;

/**
 * <code>TreeWalker</code>...
 */
public class TreeWalker {

    /**
     * filter that regulates what to be included in the aggregate
     */
    private final ItemFilterSet contentFilter = new ItemFilterSet();

    private final List<Node> ignored = new LinkedList<Node>();

    private final List<Property> binaries = new LinkedList<Property>();

    private final Set<String> ignoredPaths = new HashSet<String>();

    private int maxDepth = Integer.MAX_VALUE;

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public ItemFilterSet getContentFilter() {
        return contentFilter;
    }

    public List<Node> getIgnored() {
        return ignored;
    }

    public List<Property> getBinaries() {
        return binaries;
    }

    public Set<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public void walk(TreeWalkerListener listener, Node root, WorkspaceFilter filter) throws RepositoryException {
        listener.onWalkBegin(root);
        walk(listener, root, 0, filter);
        listener.onWalkEnd(root);
    }

    private void walk(TreeWalkerListener listener, Node node, int depth, WorkspaceFilter filter) throws RepositoryException {
        boolean included = filter.contains(node.getPath());
        if (included) {
            List<String> childNames = null;
            if (node.getPrimaryNodeType().hasOrderableChildNodes()) {
                childNames = new LinkedList<String>();
            }
            listener.onNodeBegin(node, true, depth);
            PropertyIterator pIter = node.getProperties();
            while (pIter.hasNext()) {
                Property p = pIter.nextProperty();
                if (!ignoredPaths.contains(p.getPath())) {
                    listener.onProperty(p, depth+1);
                    if (p.getType() == PropertyType.BINARY) {
                        binaries.add(p);
                    }
                }
            }
            listener.onChildren(node, depth);
            NodeIterator nIter = node.getNodes();
            int childDepth = depth+1;
            boolean hasIgnoredChildren = false;
            while (nIter.hasNext()) {
                Node child = nIter.nextNode();
                if (childNames != null) {
                    childNames.add(Text.getName(child.getPath()));
                }
                if (depth < maxDepth && contentFilter.contains(child, childDepth)) {
                    walk(listener, child, childDepth, filter);
                } else {
                    ignored.add(child);
                    hasIgnoredChildren = true;
                    listener.onNodeIgnored(child, childDepth);
                }
            }
            if (!hasIgnoredChildren) {
                // don't report child names if all children are included. so the order is already preserved
                childNames = null;
            }
            listener.onNodeEnd(node, true, depth, childNames);
        } else {
            listener.onNodeIgnored(node, depth);
        }
    }
}