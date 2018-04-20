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
package org.apache.jackrabbit.vault.packagemgr.impl.siren;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * n Entity is a URI-addressable resource that has properties and actions associated with it. It may contain
 * sub-entities and navigational links.
 * <p></p>
 * Root entities and sub-entities that are embedded representations SHOULD contain a links collection with at least one
 * item contain a rel value of self and an href attribute with a value of the entity's URI.
 * <p></p>
 * Sub-entities that are embedded links MUST contain an href attribute with a value of its URI.
 */
public interface Entity extends Link {

    /**
     * Describes the nature of an entity's content based on the current representation.
     * Possible values are implementation-dependent and should be documented. Optional.
     * @return the classes
     */
    @Nonnull
    @Override
    Set<String> getClasses();

    /**
     * A set of key-value pairs that describe the state of an entity. Optional.
     * @return the properties
     */
    @Nonnull
    Map<String, Object> getProperties();

    /**
     * A collection of related sub-entities. If a sub-entity contains an href value, it should be treated as an embedded link.
     * Clients may choose to optimistically load embedded links. If no href value exists, the sub-entity is an embedded entity
     * representation that contains all the characteristics of a typical entity. One difference is that a sub-entity MUST
     * contain a rel attribute to describe its relationship to the parent entity. Optional.
     * @return the sub entities
     */
    @Nonnull
    Iterable<Entity> getEntities();

    /**
     * A collection of items that describe navigational links, distinct from entity relationships. Link items should
     * contain a rel attribute to describe the relationship and an href attribute to point to the target URI.
     * Entities should include a link rel to self. Optional.
     * @return the links
     */
    @Nonnull
    Collection<Link> getLinks();

    /**
     * A collection of action objects. Optional.
     * @return the actions
     */
    @Nonnull
    Collection<Action> getActions();

    /**
     * Descriptive text about the entity. Optional.
     * @return the title
     */
    @Nullable
    @Override
    String getTitle();
}