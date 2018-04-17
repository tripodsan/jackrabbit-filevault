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
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

/**
 * {@code EntityBuilder}...
 */
public class EntityBuilder extends LinkBuilder {

    private Map<String, Object> props = new TreeMap<String, Object>();

    private TreeSet<Link> links = new TreeSet<Link>();

    private List<Entity> entities = new LinkedList<>();

    private TreeSet<Action> actions = new TreeSet<>();

    public EntityBuilder withProperties(Map<String, Object> properties) {
        props.putAll(properties);
        return this;
    }

    public EntityBuilder addLink(Link link) {
        links.add(link);
        return this;
    }

    public EntityBuilder addEntity(Entity entity) {
        entities.add(entity);
        return this;
    }

    public EntityBuilder addAction(Action action) {
        actions.add(action);
        return this;
    }

    public Entity build() {
        return new EntityImpl();
    }

    protected class EntityImpl extends LinkBuilder.LinkImpl implements Entity {

        @Override
        public Map<String, Object> getProperties() {
            return props;
        }

        @Override
        public Collection<Link> getLinks() {
            return links;
        }

        @Override
        public Iterable<Entity> getEntities() {
            return entities;
        }

        @Override
        public Collection<Action> getActions() {
            return actions;
        }

    }

}