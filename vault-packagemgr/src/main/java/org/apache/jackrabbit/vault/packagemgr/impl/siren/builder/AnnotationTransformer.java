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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

public class AnnotationTransformer {

    private static void addStrings(Set<String> set, Object obj) {
        if (obj == null) {
            return;
        }
        if (obj.getClass().isArray()) {
            for (Object o: ((Object[]) obj)) {
                set.add(o.toString());
            }
        } else {
            set.add(obj.toString());
        }
    }

    private static Object getValue(Object obj, Member member) {
        try {
            if (member instanceof Field) {
                return ((Field) member).get(obj);
            } else if (member instanceof Method) {
                return ((Method) member).invoke(obj);
            }
            return null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getStringValue(Object obj, Member member) {
        final Object ret = getValue(obj, member);
        return ret == null ? null : ret.toString();
    }

    private static String[] getStringValues(Object obj, Member member) {
        final Object ret = getValue(obj, member);
        if (ret instanceof String[]) {
            return (String[]) ret;
        }
        return null;
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

        return new LinkBuilder()
                .withRels(rels)
                .withHref(href);
    }

    public Link transformLink(Object model, ApiLink annotation, Member member) {
        return buildLink(annotation, getStringValue(model, member));
    }

    public Set<String> collectClasses(Object model) {
        Set<String> classes = new HashSet<>();
        for (Field field: model.getClass().getFields()) {
            ApiClass annotation = field.getAnnotation(ApiClass.class);
            if (annotation != null) {
                addStrings(classes, getValue(model, field));
            }
        }
        for (Method method: model.getClass().getMethods()) {
            ApiClass annotation = method.getAnnotation(ApiClass.class);
            if (annotation != null) {
                addStrings(classes, getValue(model, method));
            }
        }

        // todo: move somewhere generic
        ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
        if (annotation != null) {
            classes.addAll(Arrays.asList(annotation.cls()));
        }

        return classes;
    }

    public Map<String,Object> collectProperties(Object model) {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
}
