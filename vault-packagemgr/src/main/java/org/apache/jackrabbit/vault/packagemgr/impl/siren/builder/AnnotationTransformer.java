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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiEntities;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

public class AnnotationTransformer {

    private String baseHref = "";

    private Object model;

    public AnnotationTransformer withBaseHref(String baseHref) {
        this.baseHref = baseHref;
        return this;
    }

    public AnnotationTransformer withModel(Object model) {
        this.model = model;
        return this;
    }

    private Link buildLink(ApiLink annotation, String href) {
        Set<String> rels = new HashSet<>();
        for (String rel: annotation.rels()) {
            if (!rel.isEmpty()) {
                rels.add(rel);
            }
        }
        if (!annotation.value().isEmpty()) {
            rels.add(annotation.value());
        }

        if (href.isEmpty()) {
            href = baseHref;
        } else if (href.startsWith("/")) {
            href = baseHref + href;
        }
        return new LinkBuilder()
                .withRels(rels)
                .withHref(href);
    }

    public Link transformLink(ApiLink annotation, Member member) {
        return buildLink(annotation, ReflectionUtils.getStringValue(model, member));
    }

    public Collection<Link> collectLinks() {
        List<Link> links = new LinkedList<>();
        for (Field field: model.getClass().getFields()) {
            ApiLink annotation = field.getAnnotation(ApiLink.class);
            if (annotation != null) {
                links.add(buildLink(annotation, ReflectionUtils.getStringValue(model, field)));
            }
        }
        for (Method method: model.getClass().getMethods()) {
            ApiLink annotation = method.getAnnotation(ApiLink.class);
            if (annotation != null) {
                links.add(buildLink(annotation, ReflectionUtils.getStringValue(model, method)));
            }
        }
        return links;
    }

    public Set<String> collectClasses() {
        Set<String> classes = new HashSet<>();
        for (Field field: model.getClass().getFields()) {
            ApiClass annotation = field.getAnnotation(ApiClass.class);
            if (annotation != null) {
                ReflectionUtils.addStrings(classes, ReflectionUtils.getValue(model, field));
            }
        }
        for (Method method: model.getClass().getMethods()) {
            ApiClass annotation = method.getAnnotation(ApiClass.class);
            if (annotation != null) {
                ReflectionUtils.addStrings(classes, ReflectionUtils.getValue(model, method));
            }
        }

        ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
        if (annotation != null) {
            classes.addAll(Arrays.asList(annotation.cls()));
        }

        return classes;
    }

    public Map<String,Object> collectProperties() {
        Map<String, Object> properties = new HashMap<>();
        for (Field field: model.getClass().getFields()) {
            ApiProperty annotation = field.getAnnotation(ApiProperty.class);
            if (annotation != null) {
                String name = annotation.name();
                if (name.isEmpty()) {
                    name = field.getName();
                }
                ReflectionUtils.addProperty(properties, name, annotation.flatten(), ReflectionUtils.getValue(model, field));
            }
        }
        for (Method method: model.getClass().getMethods()) {
            ApiProperty annotation = method.getAnnotation(ApiProperty.class);
            if (annotation != null) {
                String name = annotation.name();
                if (name.isEmpty()) {
                    name = ReflectionUtils.methodToPropertyName(method.getName());
                }
                ReflectionUtils.addProperty(properties, name, annotation.flatten(), ReflectionUtils.getValue(model, method));
            }
        }
        return properties;
    }

    public Collection<?> collectEntities() {
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
        return ret;
    }

    public Entity build() {
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

        return builder;
    }
}
