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

import java.net.URI;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

/**
 * {@code EntityBuilder}...
 */
public class EntityBuilder {

    private TreeSet<String> classes = new TreeSet<String>();

    private Map<String, Object> props = new TreeMap<String, Object>();

    private TreeSet<Link> links = new TreeSet<Link>();

    private List<Entity> entities = new LinkedList<>();

    private TreeSet<Action> actions = new TreeSet<>();

    private Set<String> rels = new TreeSet<>();

    private String href = "";

    public EntityBuilder addClass(String className) {
        classes.add(className);
        return this;
    }

//    public EntityBuilder addProperty(String name, Calendar value) {
//        if (value != null) {
//            props.put(name, ISO8601.format(value));
//        }
//        return this;
//    }
//
//    public EntityBuilder addProperty(String name, AccessControlHandling value) {
//        if (value != null) {
//            props.put(name, value.name().toLowerCase());
//        }
//        return this;
//    }
//
    public EntityBuilder addProperty(String name, Object value) {
        if (value != null) {
            props.put(name, value);
        }
        return this;
    }

    public EntityBuilder addProperties(Map<String, Object> properties) {
        props.putAll(properties);
        return this;
    }

//    public EntityBuilder addProperty(String name, Node node, String jcrName) throws RepositoryException {
//        if (node.hasProperty(jcrName)) {
//            Property p = node.getProperty(jcrName);
//            if (p.isMultiple()) {
//                List<String> values = new LinkedList<String>();
//                for (Value v : p.getValues()) {
//                    values.add(v.getString());
//                }
//                props.put(name, values.toArray(new String[values.size()]));
//            } else {
//                props.put(name, p.getString());
//            }
//        }
//        return this;
//    }

    public EntityBuilder addLink(Link link) {
        links.add(link);
        return this;
    }

//    public EntityBuilder addLink(String rel, String href) {
//        links.add(new LinkBuilder().addRel(rel).withHref(href).build());
//        return this;
//    }

    public EntityBuilder addEntity(Entity entity) {
        entities.add(entity);
        return this;
    }

    public EntityBuilder addAction(Action action) {
        actions.add(action);
        return this;
    }

    public EntityBuilder withRels(Set<String> rels) {
        this.rels.addAll(rels);
        return this;
    }

    public EntityBuilder withHref(URI uri) {
        this.href = uri == null ? "" : uri.toString();
        return this;
    }

    public Entity build() {
        return new EntityImpl();
    }

    protected class EntityImpl implements Entity {

        @Override
        public Set<String> getClasses() {
            return classes;
        }

        @Override
        public Map<String, Object> getProperties() {
            return props;
        }

        @Override
        public Iterable<Link> getLinks() {
            return links;
        }

        @Override
        public Iterable<Entity> getEntities() {
            return entities;
        }

        @Override
        public Iterable<Action> getActions() {
            return actions;
        }

        @Override
        public Set<String> getRels() {
            return rels;
        }

        @Override
        public String getHref() {
            return href;
        }
    }

}