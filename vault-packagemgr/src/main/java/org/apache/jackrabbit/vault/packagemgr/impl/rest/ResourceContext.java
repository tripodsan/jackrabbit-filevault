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

package org.apache.jackrabbit.vault.packagemgr.impl.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.RestUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ActionInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ClassesInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.LinkInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfoLoader;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.PropertyInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.RelationInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.LinkBuilder;

public class ResourceContext {

    private ModelInfoLoader infoLoader;

    private URI baseURI;

    private URI selfURI;

    private Set<String> classes = new HashSet<>();

    private String pseudoClass;

    private ModelInfo info;

    private Object model;

    private Object parentModel;

    public ResourceContext withInfoLoader(ModelInfoLoader infoLoader) {
        this.infoLoader = infoLoader;
        return this;
    }

    public ResourceContext withInfo(ModelInfo info) {
        this.info = info;
        return this;
    }

    public ResourceContext withSelfURI(URI selfURI) {
        this.selfURI = selfURI;
        return this;
    }

    public ResourceContext withBaseURI(URI baseURI) {
        this.baseURI = baseURI;
        return this;
    }

    public ResourceContext withModel(Object model) {
        this.model = model;
        return this;
    }

    private ResourceContext withParentModel(Object parentModel) {
        this.parentModel = parentModel;
        return this;
    }

    private Collection<Link> collectLinks() throws URISyntaxException {
        boolean hasHref = false;
        if (info.getHref() != null) {
            final Object ret = info.getHref().getValue(model);
            if (ret instanceof URI) {
                hasHref = true;
                selfURI = (URI) ret;
            } else if (ret != null) {
                hasHref = true;
                selfURI = new URI(RestUtils.resolveHref(baseURI, ret.toString()));
            }
        }

        // first get all links w/o any href resolution
        List<LinkBuilder> links = new LinkedList<>();
        LinkBuilder selfLink = null;
        for (LinkInfo info: info.getLinks()) {
            for (LinkBuilder link: info.createSirenLinkBuilders(model)) {
                if (link.getRels().contains(Link.SELF)) {
                    selfLink = link;
                } else {
                    links.add(link);
                }
            }
        }

        if (selfLink == null) {
            if (info.isSelfLink()) {
                if (!hasHref) {
                    selfURI = new URI(RestUtils.resolveHref(baseURI, info.getRelPath()));
                }
                selfLink = new LinkBuilder()
                        .addRel(Link.SELF)
                        .withHref(selfURI.toString());
            }
        } else if (hasHref) {
            // adjust self link with selfURI set by href
            selfLink = selfLink.withHref(RestUtils.resolveHref(selfURI, selfLink.getHref()));
        } else {
            // no href, so define self uri via self ref
            String newSelfRef = RestUtils.resolveHref(baseURI, selfLink.getHref());
            selfURI = new URI(newSelfRef);
            selfLink = selfLink.withHref(newSelfRef);
        }
        List<Link> ret = new ArrayList<>(links.size());
        for (LinkBuilder builder: links) {
            ret.add(builder
                    .withHref(RestUtils.resolveHref(selfURI, builder.getHref()))
                    .build()
            );
        }
        if (selfLink != null) {
            ret.add(selfLink.build());
        }
        return ret;
    }

    Set<String> collectClasses() {
        for (ClassesInfo classesInfo: info.getClasses()) {
            ReflectionUtils.addStrings(classes, classesInfo.getValue(model));
        }
        classes.addAll(info.getGlobalClasses());
        pseudoClass = parentModel == null ? ApiProperty.CONTEXT_ENTITY : ApiProperty.CONTEXT_SUB_ENTITY;
        return classes;
    }

    private Set<String> collectRels() {
        Set<String> rels = new HashSet<>();
        for (RelationInfo rel: info.getRelations()) {
            ReflectionUtils.addStrings(rels, rel.getValue(model));
        }
        return rels;
    }

    Map<String,Object> collectProperties() {
        Map<String, Object> properties = new HashMap<>();
        for (PropertyInfo prop: info.getProperties().values()) {
            if (!prop.isActive(classes, pseudoClass)) {
                continue;
            }

            Object value = prop.getValue(model);
            if (value == null) {
                continue;
            }
            ReflectionUtils.addProperty(properties, prop.getName(), prop.isFlatten(), value);
        }
        return properties;
    }

    Iterable<?> collectEntities() {
        Collection<?> ret = null;
        if (info.getEntities() != null) {
            ret = info.getEntities().getValues(model);
        }
        return ret == null ? Collections.emptyList() : ret;
    }

    private Collection<Action> collectActions() {
        List<Action> actions = new ArrayList<>(info.getActions().size());
        for (ActionInfo action: info.getActions().values()) {
            // skip default action
            if ("default".equals(action.getName())) {
                continue;
            }
            actions.add(action.createSirenAction(selfURI));
        }
        return actions;
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final Action.Method method = Action.Method.valueOf(req.getMethod());
        ActionInfo action = info.findAction(method, req.getContentType());
        if (action == null) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        new ActionContext()
                .withContext(this)
                .withActionInfo(action)
                .withModelInfo(info)
                .withResource(model)
                .withRequest(req)
                .withResponse(resp)
                .execute();
    }


    public Entity buildEntity() throws IOException {
        try {
            EntityBuilder builder = new EntityBuilder();

            // links
            for (Link link: collectLinks()) {
                builder.addLink(link);
            }

            // class
            for (String clazz: collectClasses()){
                builder.addClass(clazz);
            }

            // properties
            builder.withProperties(collectProperties());

            // entities
            for (Object entity: collectEntities()) {
                Entity e = new ResourceContext()
                        .withModel(entity)
                        .withInfo(infoLoader.load(entity.getClass()))
                        .withInfoLoader(infoLoader)
                        .withParentModel(model)
                        .withBaseURI(baseURI)
                        .buildEntity();
                builder.addEntity(e);
            }

            // actions
            for (Action a: collectActions()) {
                builder.addAction(a);
            }

            // rels
            if (parentModel != null) {
                builder.withRels(collectRels());
            }
            return builder.build();

        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
