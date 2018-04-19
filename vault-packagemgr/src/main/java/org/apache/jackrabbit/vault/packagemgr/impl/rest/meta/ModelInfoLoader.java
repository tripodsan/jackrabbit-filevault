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

    private Map<Class<?>, ModelInfo> cache = new ConcurrentHashMap<>();

//    private String resolveHref(URI base, String href) throws URISyntaxException {
//        if (href.isEmpty()) {
//            return base.toString();
//        } else if (href.startsWith("/") || href.startsWith("?") ) {
//            return base.toString() + href;
//        }
//        return href;
//    }

//    private LinkBuilder buildLink(ApiLink annotation, String href) throws URISyntaxException {
//        return new LinkBuilder()
//                .withHref(href)
//                .withRels(annotation.value())
//                .withClasses(annotation.classes())
//                .withTitle(annotation.title())
//                .withType(annotation.type());
//    }

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
                .withMethod(method);

        for (ApiField field: action.fields()) {
            builder.addField(ActionInfo.createField(field, String.class));
        }

        return builder.build();
    }

//
//    public Collection<Link> collectLinks() throws URISyntaxException {
//        // first get all links w/o any href resolution
//        Member[] members = ReflectionUtils.getFieldsAndMethods(model.getClass());
//        List<LinkBuilder> links = new ArrayList<>(members.length);
//        LinkBuilder selfLink = null;
//        boolean hasHref = false;
//        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
//            ApiLink annotation = ((AnnotatedElement) member).getAnnotation(ApiLink.class);
//            if (annotation != null) {
//                String[] values = ReflectionUtils.getStringValues(model, member);
//                if (values != null) {
//                    for (String href: values) {
//                        LinkBuilder link = buildLink(annotation, href);
//                        if (link.getRels().contains(ApiLink.SELF)) {
//                            selfLink = link;
//                        } else {
//                            links.add(link);
//                        }
//                    }
//                }
//            }
//            ApiHref hrefAnnotation = ((AnnotatedElement) member).getAnnotation(ApiHref.class);
//            if (hrefAnnotation != null) {
//                final Object ret = ReflectionUtils.getValue(model, member);
//                if (ret instanceof URI) {
//                    hasHref = true;
//                    selfURI = (URI) ret;
//                } else if (ret != null) {
//                    hasHref = true;
//                    selfURI = new URI(resolveHref(baseURI, ret.toString()));
//                }
//            }
//        }
//        if (selfLink == null) {
//            ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
//            if (annotation != null && annotation.selfLink()) {
//                if (!hasHref) {
//                    selfURI = new URI(resolveHref(baseURI, annotation.relPath()));
//                }
//                selfLink = new LinkBuilder()
//                        .addRel(ApiLink.SELF)
//                        .withHref(selfURI.toString());
//            }
//        } else if (hasHref) {
//            // adjust self link with selfURI set by href
//            selfLink = selfLink.withHref(resolveHref(selfURI, selfLink.getHref()));
//        } else {
//            // no href, so define self uri via self ref
//            String newSelfRef = resolveHref(baseURI, selfLink.getHref());
//            selfURI = new URI(newSelfRef);
//            selfLink = selfLink.withHref(newSelfRef);
//        }
//        List<Link> ret = new ArrayList<>(links.size());
//        for (LinkBuilder builder: links) {
//            ret.add(builder
//                    .withHref(resolveHref(selfURI, builder.getHref()))
//                    .build()
//            );
//        }
//        if (selfLink != null) {
//            ret.add(selfLink.build());
//        }
//        return ret;
//    }
//
//    public Set<String> collectClasses() {
//        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
//            ApiClass annotation = ((AnnotatedElement) member).getAnnotation(ApiClass.class);
//            if (annotation != null) {
//                ReflectionUtils.addStrings(classes, ReflectionUtils.getValue(model, member));
//            }
//        }
//        ApiModel annotation = model.getClass().getAnnotation(ApiModel.class);
//        if (annotation != null) {
//            classes.addAll(Arrays.asList(annotation.classes()));
//        }
//        pseudoClass = parentModel == null ? ApiProperty.CONTEXT_ENTITY : ApiProperty.CONTEXT_SUB_ENTITY;
//        return classes;
//    }

//    private Set<String> collectRels() {
//        Set<String> rels = new HashSet<>();
//        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
//            ApiRelation annotation = ((AnnotatedElement) member).getAnnotation(ApiRelation.class);
//            if (annotation != null) {
//                ReflectionUtils.addStrings(rels, ReflectionUtils.getValue(model, member));
//            }
//        }
//        return rels;
//    }
//
//    public Map<String,Object> collectProperties() {
//        Map<String, Object> properties = new HashMap<>();
//        for (Member member: ReflectionUtils.getFieldsAndMethods(model.getClass())) {
//            ApiProperty annotation = ((AnnotatedElement) member).getAnnotation(ApiProperty.class);
//            if (annotation != null) {
//                if (annotation.context().length > 0 && !classes.isEmpty()) {
//                    boolean include = false;
//                    for (String ctx: annotation.context()) {
//                        if (classes.contains(ctx) || ctx.equals(pseudoClass)) {
//                            include = true;
//                            break;
//                        }
//                    }
//                    if (!include) {
//                        continue;
//                    }
//                }
//
//                Object value = ReflectionUtils.getValue(model, member);
//                if (value == null) {
//                    continue;
//                }
//                String name = annotation.name();
//                if (name.isEmpty()) {
//                    name = annotation.value();
//                }
//                if (name.isEmpty()) {
//                    if (member instanceof Method) {
//                        name = ReflectionUtils.methodToPropertyName(member.getName());
//                    } else {
//                        name = member.getName();
//                    }
//                }
//                ReflectionUtils.addProperty(properties, name, annotation.flatten(), value);
//            }
//        }
//        return properties;
//    }

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

//    public Iterable<?> collectEntities() {
//        Collection<?> ret = null;
//        for (Method method: model.getClass().getMethods()) {
//            ApiEntities annotation = method.getAnnotation(ApiEntities.class);
//            if (annotation != null) {
//                if (ret != null) {
//                    throw new IllegalArgumentException("Model defines multiple entities annotations");
//                }
//                try {
//                    ret = (Collection<?>) method.invoke(model);
//                } catch (IllegalAccessException | InvocationTargetException e) {
//                    throw new IllegalArgumentException(e);
//                }
//            }
//        }
//        return ret == null ? Collections.emptyList() : ret;
//    }

//    public Collection<ActionInfoContext> collectActions() throws URISyntaxException {
//        List<ActionInfoContext> actions = new LinkedList<>();
//        for (Method method: model.getClass().getMethods()) {
//            ApiAction annotation = method.getAnnotation(ApiAction.class);
//            if (annotation != null) {
//                actions.add(buildAction(annotation, method));
//            }
//        }
//        return actions;
//    }
//

    @Nonnull
    public ModelInfo load(@Nonnull Class<?> modelClass) {
        ModelInfo info = cache.get(modelClass);
        if (info == null) {
            info = buildInfo(modelClass);
            cache.put(modelClass, info);
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

        for (Method method: modelClass.getMethods()) {
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

        for (Field field: modelClass.getFields()) {
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

        return builder.build();
    }

}
