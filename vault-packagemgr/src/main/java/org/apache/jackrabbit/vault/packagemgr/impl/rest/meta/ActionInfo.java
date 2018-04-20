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

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.jackrabbit.vault.packagemgr.impl.RestUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.ResourceContext;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;

public class ActionInfo {

    private String name;

    private Action.Method httpMethod;

    private String title;

    private String href;

    private String contentType = "";

    private Method method;

    private String[] context;

    private List<Field> sirenFields = new LinkedList<>();

    private Map<String, ParameterInfo> fields = new TreeMap<>();

    private ActionInfo() {
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public String getTitle() {
        return title;
    }

    public String getHref() {
        return href;
    }

    public List<Field> getSirenFields() {
        return sirenFields;
    }

    public Map<String, ParameterInfo> getFields() {
        return fields;
    }

    public Action.Method getHttpMethod() {
        return httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public String[] getContext() {
        return context;
    }

    public boolean isActive(Set<String> classes, String pseudoClass) {
        if (context.length == 0 || classes.isEmpty()) {
            return true;
        }
        for (String ctx: context) {
            if (classes.contains(ctx) || ctx.equals(pseudoClass)) {
                return true;
            }
        }
        return false;
    }

    public Action createSirenAction(URI selfURI) {
        return new ActionBuilder()
                .withName(name)
                .withMethod(Action.Method.valueOf(httpMethod.name()))
                .withTitle(title)
                .withHref(RestUtils.resolveHref(selfURI, href))
                .withType(contentType)
                .withFields(sirenFields)
                .build();
    }

    public static class Builder {

        private ActionInfo info;

        public Builder() {
            info = new ActionInfo();
        }

        public Builder withMethod(Method method) {
            info.method = method;
            return this;
        }

        public Builder withName(String name) {
            info.name = name;
            return this;
        }

        public Builder withHttpMethod(Action.Method httpMethod) {
            info.httpMethod = httpMethod;
            return this;
        }

        public Builder withTitle(String title) {
            info.title = title;
            return this;
        }

        public Builder withHref(String href) {
            info.href = href;
            return this;
        }

        public Builder withContentType(String type) {
            info.contentType = type == null ? "" : type;
            return this;
        }

        public Builder addField(Field field) {
            info.sirenFields.add(field);
            return this;
        }

        public Builder withFields(List<Field> fields) {
            info.sirenFields = fields;
            return this;
        }

        public Builder withContext(String[] context) {
            info.context = context;
            return this;
        }

        private void addField(String name, int idx) {
            info.fields.put(name, new ParameterInfo(name, idx));
        }

        private void createMethodParameterFields() {
            Class<?>[] params = info.method.getParameterTypes();
            Annotation[][] annotations = info.method.getParameterAnnotations();
            boolean canHandleBody = false;

            for (int i = 0; i < params.length; i++) {
                Class<?> type = params[i];
                ApiField fld = null;
                for (Annotation a : annotations[i]) {
                    if (a instanceof ApiField) {
                        if (fld != null) {
                            throw new IllegalArgumentException(i + ". parameter has multiple ApiField annotations: " + info.method);
                        }
                        fld = (ApiField) a;
                    }
                }
                if (type.isAssignableFrom(HttpServletRequest.class)) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_REQUEST)) {
                        throw new IllegalArgumentException("only 1 HttpServletRequest possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_REQUEST, i);
                    canHandleBody = true;

                } else if (type.isAssignableFrom(ResourceContext.class)) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_CONTEXT)) {
                        throw new IllegalArgumentException("only 1 ResourceContext possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_CONTEXT, i);
                    canHandleBody = true;

                } else if (type.isAssignableFrom(HttpServletResponse.class)) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_RESPONSE)) {
                        throw new IllegalArgumentException("only 1 HttpServletResponse possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_RESPONSE, i);

                } else if (type.isAssignableFrom(FileItem[].class)) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_FORM)) {
                        throw new IllegalArgumentException("only 1 FileItem[] possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_FORM, i);
                    info.contentType = Action.TYPE_MULTIPART_FORM_DATA;
                    info.httpMethod = Action.Method.POST;
                    canHandleBody = true;

                } else if (type.isAssignableFrom(JsonObject.class)) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_JSON_BODY)) {
                        throw new IllegalArgumentException("only 1 JsonObject possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_JSON_BODY, i);
                    info.contentType = Action.TYPE_JSON;
                    canHandleBody = true;

                } else if (type.isAssignableFrom(InputStream.class) && fld == null) {
                    if (info.fields.containsKey(ParameterInfo.TYPE_STREAM_BODY)) {
                        throw new IllegalArgumentException("only 1 InputStream body possible for method " + info.method);
                    }
                    if (info.fields.containsKey(ParameterInfo.TYPE_FORM) || info.fields.containsKey(ParameterInfo.TYPE_JSON_BODY)) {
                        throw new IllegalArgumentException("only 1 body consuming parameter possible for method " + info.method);
                    }
                    addField(ParameterInfo.TYPE_STREAM_BODY, i);
                    info.contentType = Action.TYPE_APPLICATION_OCTET_STREAM;
                    canHandleBody = true;

                } else if (fld != null) {
                    Field f = createField(fld, type);
                    addField(f);
                    info.fields.put(f.getName(), new ParameterInfo(f, type, i));
                    if ("file".equals(f.getType())) {
                        info.contentType = Action.TYPE_MULTIPART_FORM_DATA;
                        info.httpMethod = Action.Method.POST;
                    }
                }
            }
            if (!info.sirenFields.isEmpty() && info.fields.isEmpty() && !canHandleBody) {
                throw new IllegalArgumentException("action fields defined but no parameter can handle them " + info.method);
            }
        }

        public ActionInfo build() {
            if (info.method != null) {
                createMethodParameterFields();
            }

            try {
                return info;
            } finally {
                info = null;
            }
        }
    }


    @Nonnull
    static Field createField(@Nonnull ApiField field, @Nonnull Class<?> type) {
        String fieldName = field.name();
        if (fieldName.isEmpty()) {
            fieldName = field.value();
        }
        ApiField.Type fieldType = field.type();
        if (fieldType == ApiField.Type.AUTO) {
            if (type.isArray()) {
                type = type.getComponentType();
            }

            if (type.isAssignableFrom(String.class)) {
                fieldType = ApiField.Type.TEXT;
            } else if (type.isAssignableFrom(boolean.class)) {
                fieldType = ApiField.Type.CHECKBOX;
            } else if (type.isAssignableFrom(Boolean.class)) {
                fieldType = ApiField.Type.CHECKBOX;
            } else if (type.isAssignableFrom(Number.class)) {
                fieldType = ApiField.Type.NUMBER;
            } else if (type.isAssignableFrom(int.class)) {
                fieldType = ApiField.Type.NUMBER;
            } else if (type.isAssignableFrom(long.class)) {
                fieldType = ApiField.Type.NUMBER;
            } else if (type.isAssignableFrom(float.class)) {
                fieldType = ApiField.Type.NUMBER;
            } else if (type.isAssignableFrom(double.class)) {
                fieldType = ApiField.Type.NUMBER;
            } else if (type.isAssignableFrom(InputStream.class)) {
                fieldType = ApiField.Type.FILE;
            } else if (type.isAssignableFrom(File.class)) {
                fieldType = ApiField.Type.FILE;
            } else if (type.isAssignableFrom(Calendar.class)) {
                fieldType = ApiField.Type.DATETIME;
            } else {
                fieldType = ApiField.Type.TEXT;
            }
        }
        return new FieldBuilder()
                .withName(fieldName)
                .withType(fieldType.toString())
                .withTitle(field.title())
                .withValue(field.defaultValue())
                .build();
    }

}
