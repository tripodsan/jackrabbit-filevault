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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.fs.impl.io.AggregateWalkListener;

/**
 * {@code TreeVisitor}...
 */
public interface TreeWalkerListener extends AggregateWalkListener {

    /**
     * Invoked when a node finished traversing
     *
     * @param node     the node that is finished traversing
     * @param included indicates if the node is included in the aggregate. If
     *                 <code>false</code> it's just a traversed intermediate node.
     * @param depth    the relative depth of the node in respect to the tree root node.
     * @param childNodeNames name of the child nodes if this node has orderable child nodes
     * @throws javax.jcr.RepositoryException if a repository error occurs.
     */
    void onNodeEnd(Node node, boolean included, int depth, Iterable<String> childNodeNames)
            throws RepositoryException;

}