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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelInfo {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(ModelInfo.class);

    private Class<?> modelClass;

    private Set<String> globalClasses = Collections.emptySet();

    private String relPath;

    private boolean selfLink;

    private EntitiesInfo entities;

    private TreeMap<String, ActionInfo> actions = new TreeMap<>();

    private List<ClassesInfo> classes = new LinkedList<>();

    private List<RelationInfo> relations = new LinkedList<>();

    private TreeMap<String, PropertyInfo> properties = new TreeMap<>();

    private HrefInfo href;

    private List<LinkInfo> links = new LinkedList<>();

    public Class<?> getModelClass() {
        return modelClass;
    }

    public Set<String> getGlobalClasses() {
        return globalClasses;
    }

    public String getRelPath() {
        return relPath;
    }

    public boolean isSelfLink() {
        return selfLink;
    }

    public EntitiesInfo getEntities() {
        return entities;
    }

    public TreeMap<String, ActionInfo> getActions() {
        return actions;
    }

    public List<ClassesInfo> getClasses() {
        return classes;
    }

    public List<RelationInfo> getRelations() {
        return relations;
    }

    public TreeMap<String, PropertyInfo> getProperties() {
        return properties;
    }

    public HrefInfo getHref() {
        return href;
    }

    public List<LinkInfo> getLinks() {
        return links;
    }


    public ActionInfo findAction(Action.Method method, String contentType) {
        if (contentType == null) {
            contentType = "";
        }
        int idx = contentType.indexOf(';');
        if (idx > 0) {
            contentType = contentType.substring(0, idx);
        }
        ActionInfo methodMatch = null;
        for (ActionInfo a: actions.values()) {
            if (a.getMethod() == null) {
                // skip synthetic actions
                continue;
            }
            if (method == a.getHttpMethod()) {
                methodMatch = a;
                if (contentType.equals(a.getContentType())) {
                    return a;
                }
            }
        }
        if (methodMatch == null) {
            log.warn("no action found for {} with type {}", method, contentType);
        } else {
            log.warn("no action only matched method {} but not content type {}", method, contentType);
        }
        return methodMatch;
    }

    public static class Builder {

        private ModelInfo info;

        public Builder() {
            this.info = new ModelInfo();
        }

        public Builder withModelClass(Class<?> modelClass) {
            info.modelClass = modelClass;
            return this;
        }

        public Builder withClasses(Collection<String> classes) {
            info.globalClasses = Collections.unmodifiableSet(new HashSet<>(classes));
            return this;
        }

        public Builder withRelPath(String relPath) {
            info.relPath = relPath;
            return this;
        }

        public Builder withSelfLink(boolean selfLink) {
            info.selfLink = selfLink;
            return this;
        }

        public Builder withEntities(EntitiesInfo entities) {
            if (info.entities != null) {
                throw new IllegalArgumentException("Model defines multiple entities annotations: " + info.modelClass);
            }
            info.entities = entities;
            return this;
        }

        public Builder addAction(ActionInfo action) {
            if (info.actions.containsKey(action.getName())) {
                throw new IllegalArgumentException("Action names must be unique " + action.getName() + " in " + info.modelClass);
            }
            info.actions.put(action.getName(), action);
            return this;
        }

        public Builder addClass(ClassesInfo clazz) {
            info.classes.add(clazz);
            return this;
        }

        public Builder addRelation(RelationInfo rel) {
            info.relations.add(rel);
            return this;
        }

        public Builder addProperty(PropertyInfo property) {
            if (info.properties.containsKey(property.getName())) {
                throw new IllegalArgumentException("Property names must be unique " + property.getName() + " in " + info.modelClass);
            }
            info.properties.put(property.getName(), property);
            return this;
        }

        public Builder withHref(HrefInfo href) {
            if (info.href != null) {
                throw new IllegalArgumentException("Model defines multiple href annotations: " + info.modelClass);
            }
            info.href = href;
            return this;
        }

        public Builder addLink(LinkInfo link) {
            info.links.add(link);
            return this;
        }
        public ModelInfo build() {
            try {
                return info;
            } finally {
                info = null;
            }
        }


    }
}
