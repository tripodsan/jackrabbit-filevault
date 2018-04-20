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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiActionReference;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiEntities;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiHref;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiRelation;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;

public class ModelInfoLoader {

    private static final ModelInfo CYCLE_GUARD = new ModelInfo.Builder().build();

    private Map<Class<?>, ModelInfo> cache = new ConcurrentHashMap<>();

    private LinkInfo buildLink(ApiLink annotation, Member member) {
        return new LinkInfo.Builder()
                .withMember(member)
                .withRels(Arrays.asList(annotation.value()))
                .withClasses(Arrays.asList(annotation.classes()))
                .withTitle(annotation.title())
                .withType(annotation.type())
                .build();
    }

    private ActionInfo buildAction(ApiAction action, Method method) {
        String name = action.name();
        if (name.isEmpty()) {
            name = action.value();
        }
        if (name.isEmpty()) {
            name = ReflectionUtils.methodToActionName(method.getName());
        }
        ActionInfo.Builder builder = new ActionInfo.Builder()
                .withName(name)
                .withHttpMethod(Action.Method.valueOf(action.method().name()))
                .withTitle(action.title())
                .withHref(action.href())
                .withContentType(action.type())
                .withMethod(method)
                .withContext(action.context());

        for (ApiField field : action.fields()) {
            builder.addField(ActionInfo.createField(field, String.class));
        }

        return builder.build();
    }

    private PropertyInfo buildPropertyInfo(ApiProperty annotation, Member member) {
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
        return new PropertyInfo(name, member, annotation.flatten(), annotation.value(), annotation.context());
    }

    @Nonnull
    public ModelInfo load(@Nonnull Class<?> modelClass) {
        ModelInfo info = cache.get(modelClass);
        if (info == null) {
            cache.put(modelClass, CYCLE_GUARD);
            info = buildInfo(modelClass);
            cache.put(modelClass, info);
        } else if (info == CYCLE_GUARD) {
            throw new IllegalArgumentException("cyclic reference: " + modelClass);
        }
        return info;
    }

    private ModelInfo buildInfo(Class<?> modelClass) {
        ApiModel model = modelClass.getAnnotation(ApiModel.class);
        if (model == null) {
            throw new IllegalArgumentException("Missing ApiModel annotation in " + modelClass);
        }

        ModelInfo.Builder builder = new ModelInfo.Builder()
                .withModelClass(modelClass)
                .withClasses(Arrays.asList(model.classes()))
                .withRelPath(model.relPath())
                .withSelfLink(model.selfLink());

        for (Method method : modelClass.getMethods()) {
            ApiEntities apiEntities = method.getAnnotation(ApiEntities.class);
            if (apiEntities != null) {
                builder.withEntities(new EntitiesInfo(method));
            }

            ApiAction apiAction = method.getAnnotation(ApiAction.class);
            if (apiAction != null) {
                builder.addAction(buildAction(apiAction, method));
            }

            ApiClass apiClass = method.getAnnotation(ApiClass.class);
            if (apiClass != null) {
                builder.addClass(new ClassesInfo(method));
            }

            ApiRelation apiRelation = method.getAnnotation(ApiRelation.class);
            if (apiRelation != null) {
                builder.addRelation(new RelationInfo(method));
            }

            ApiProperty apiProperty = method.getAnnotation(ApiProperty.class);
            if (apiProperty != null) {
                builder.addProperty(buildPropertyInfo(apiProperty, method));
            }

            ApiHref apiHref = method.getAnnotation(ApiHref.class);
            if (apiHref != null) {
                builder.withHref(new HrefInfo(method));
            }

            ApiLink apiLink = method.getAnnotation(ApiLink.class);
            if (apiLink != null) {
                builder.addLink(buildLink(apiLink, method));
            }

        }

        for (Field field : modelClass.getFields()) {
            ApiClass apiClass = field.getAnnotation(ApiClass.class);
            if (apiClass != null) {
                builder.addClass(new ClassesInfo(field));
            }

            ApiRelation apiRelation = field.getAnnotation(ApiRelation.class);
            if (apiRelation != null) {
                builder.addRelation(new RelationInfo(field));
            }

            ApiProperty apiProperty = field.getAnnotation(ApiProperty.class);
            if (apiProperty != null) {
                builder.addProperty(buildPropertyInfo(apiProperty, field));
            }

            ApiHref apiHref = field.getAnnotation(ApiHref.class);
            if (apiHref != null) {
                builder.withHref(new HrefInfo(field));
            }

            ApiLink apiLink = field.getAnnotation(ApiLink.class);
            if (apiLink != null) {
                builder.addLink(buildLink(apiLink, field));
            }
        }

        for (ApiActionReference ref : model.actions()) {
            ModelInfo refInfo = load(ref.model());
            ActionInfo action = refInfo.getActions().get(ref.name());
            if (action == null) {
                throw new IllegalArgumentException(String.format("referenced action in %s with %s does not exist. %s", ref.name(), ref.model(), modelClass));
            }
            builder.addAction(new ActionInfo.Builder()
                    .withContentType(action.getContentType())
                    .withHref(action.getHref())
                    .withHttpMethod(action.getHttpMethod())
                    .withName(action.getName())
                    .withTitle(action.getTitle())
                    .withFields(action.getSirenFields())
                    .withContext(action.getContext())
                    .build()
            );
        }
        return builder.build();
    }

}
