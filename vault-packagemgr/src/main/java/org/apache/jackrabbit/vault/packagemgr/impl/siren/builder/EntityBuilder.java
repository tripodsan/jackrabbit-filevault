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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

/**
 * {@code EntityBuilder}...
 */
public class EntityBuilder extends LinkBuilder implements Entity {

    private TreeSet<String> classes = new TreeSet<String>();

    private Properties props = new Properties();

    private TreeSet<Link> links = new TreeSet<Link>();

    private List<Entity> entities = new LinkedList<Entity>();

    private List<Action> actions = new LinkedList<Action>();

    public EntityBuilder addClass(String className) {
        classes.add(className);
        return this;
    }

    public EntityBuilder addProperty(String name, Object value) {
        props.put(name, value);
        return this;
    }

    public EntityBuilder addLink(Link link) {
        links.add(link);
        return this;
    }

    public EntityBuilder addLink(String rel, String href) {
        links.add(new LinkBuilder().addRel(rel).setHref(href));
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
        return this;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Properties getProperties() {
        return props;
    }

    public Iterable<Link> getLinks() {
        return links;
    }

    public Iterable<Entity> getEntities() {
        return entities;
    }

    public Iterable<Action> getActions() {
        return actions;
    }

}