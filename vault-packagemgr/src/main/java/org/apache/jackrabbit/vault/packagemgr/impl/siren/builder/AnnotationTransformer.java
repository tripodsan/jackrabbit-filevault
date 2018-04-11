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

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiEntities;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

public class AnnotationTransformer {

    private URI selfURI;

    private Object model;

    private Object parentModel;

    public AnnotationTransformer withSelfURI(URI selfURI) {
        this.selfURI = selfURI;
        return this;
    }

    public AnnotationTransformer withModel(Object model) {
        this.model = model;
        return this;
    }

    private AnnotationTransformer withParentModel(Object parentModel) {
        this.parentModel = parentModel;
        return this;
    }

    private String resolveHref(String href) throws URISyntaxException {
        if (href.isEmpty()) {
            return selfURI.toString();
        } else if (href.startsWith("/") || href.startsWith("?") ) {
            return selfURI.toString() + href;
        }
        return href;
    }

    private Link buildLink(ApiLink annotation, String href) throws URISyntaxException {
        Set<String> rels = new HashSet<>();
        for (String rel: annotation.value()) {
            if (!rel.isEmpty()) {
                rels.add(rel);
            }
        }
        return new LinkBuilder()
                .withRels(rels)
                .withHref(resolveHref(href))
                .build();
    }

    private Action buildAction(ApiAction action, Method method) throws URISyntaxException {
        String name = action.name();
        if (name.isEmpty()) {
            name = action.value();
        }
        if (name.isEmpty()) {
            name = ReflectionUtils.methodToActionName(method.getName());
        }
        ActionBuilder builder = new ActionBuilder()
                .withName(name)
                .withMethod(Action.Method.valueOf(action.method().name()))
                .withTitle(action.title())
                .withHref(resolveHref(action.href()))
                .withType(action.type());
        for (ApiField field: action.fields()) {
            String fieldName = field.name();
            if (fieldName.isEmpty()) {
                fieldName = field.value();
            }
            builder.addField(new FieldBuilder()
                    .withName(fieldName)
                    .withType(field.type().name())
                    .withTitle(field.title())
                    .withValue(field.defaultValue())
                    .build()
            );
        }
        return builder.build();
    }


    public Collection<Link> collectLinks() throws URISyntaxException {
        Member[] members = ReflectionUtils.getFieldsAndMethods(model.getClass());
        List<Link> links = new ArrayList<>(members.length + 1);
        Link selfLink = null;
        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
            ApiLink annotation = ((AnnotatedElement) member).getAnnotation(ApiLink.class);
            if (annotation != null) {
                String[] values = ReflectionUtils.getStringValues(model, member);
                if (values != null) {
                    for (String href: values) {
                        Link link = buildLink(annotation, href);
                        if (link.getRels().contains(ApiLink.SELF)) {
                            selfLink = link;
                        } else {
                            links.add(link);
                        }
                    }
                }
            }
        }
        if (selfLink == null) {
            ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
            if (annotation != null && annotation.selfLink()) {
                selfLink = new LinkBuilder()
                        .addRel(ApiLink.SELF)
                        .withHref(selfURI.toString())
                        .build();
            }
        }
        if (selfLink != null) {
            links.add(selfLink);
        }
        return links;
    }

    public Set<String> collectClasses() {
        Set<String> classes = new HashSet<>();
        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
            ApiClass annotation = ((AnnotatedElement) member).getAnnotation(ApiClass.class);
            if (annotation != null) {
                ReflectionUtils.addStrings(classes, ReflectionUtils.getValue(model, member));
            }
        }
        ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
        if (annotation != null) {
            classes.addAll(Arrays.asList(annotation.classes()));
        }

        return classes;
    }

    public Map<String,Object> collectProperties() {
        Map<String, Object> properties = new HashMap<>();
        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
            ApiProperty annotation = ((AnnotatedElement) member).getAnnotation(ApiProperty.class);
            if (annotation != null) {
                if (parentModel == null && annotation.context() == ApiProperty.Context.INLINE) {
                    continue;
                }
                if (parentModel != null && annotation.context() == ApiProperty.Context.ENTITY) {
                    continue;
                }
                Object value = ReflectionUtils.getValue(model, member);
                if (value == null) {
                    continue;
                }
                String name = annotation.name();
                if (name.isEmpty()) {
                    name = annotation.value();
                }
                if (name.isEmpty()) {
                    if (member instanceof Method) {
                        name = ReflectionUtils.methodToPropertyName(member.getName());
                    } else {
                        name = member.getName();
                    }
                }
                ReflectionUtils.addProperty(properties, name, annotation.flatten(), value);
            }
        }
        return properties;
    }

    public Iterable<?> collectEntities() {
        Collection<?> ret = null;
        for (Method method: model.getClass().getMethods()) {
            ApiEntities annotation = method.getAnnotation(ApiEntities.class);
            if (annotation != null) {
                if (ret != null) {
                    throw new IllegalArgumentException("Model defines multiple entities annotations");
                }
                try {
                    ret = (Collection<?>) method.invoke(model);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return ret == null ? Collections.emptyList() : ret;
    }

    public Collection<Action> collectActions() throws URISyntaxException {
        List<Action> actions = new LinkedList<>();
        for (Method method: model.getClass().getMethods()) {
            ApiAction annotation = method.getAnnotation(ApiAction.class);
            if (annotation != null) {
                actions.add(buildAction(annotation, method));
            }
        }
        return actions;
    }

    public Entity build() throws IOException {
        try {
            // class
            EntityBuilder builder = new EntityBuilder();
            for (String clazz: collectClasses()){
                builder.addClass(clazz);
            }

            // properties
            builder.addProperties(collectProperties());

            // links
            for (Link link: collectLinks()) {
                builder.addLink(link);
            }

            // entities
            for (Object entity: collectEntities()) {
                Entity e = new AnnotationTransformer()
                        .withModel(entity)
                        .withParentModel(model)
                        .withSelfURI(selfURI)
                        .build();
                builder.addEntity(e);
            }

            // actions
            for (Action a: collectActions()) {
                builder.addAction(a);
            }

            return builder.build();

        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
