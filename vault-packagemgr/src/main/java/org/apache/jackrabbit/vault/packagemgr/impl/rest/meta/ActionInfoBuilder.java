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
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUpload;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;

public class ActionInfoBuilder {

    private ActionBuilder sirenBuilder;

    private Method method;

    private Map<String, FieldInfo> fields = new TreeMap<>();

    private int httpRequestIdx = -1;

    private int httpResponseIdx = -1;

    private int inputStreamBodyIdx = -1;

    private int fileUploadIdx = -1;

    private int jsonBodyIdx = -1;

    public ActionInfoBuilder withMethod(Method method) {
        this.method = method;
        return this;
    }

    public ActionInfoBuilder withSirenBuilder(ActionBuilder sirenBuilder) {
        this.sirenBuilder = sirenBuilder;
        return this;
    }

    public ActionInfo build() {
        Class<?>[] params = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        boolean canHandleBody = false;

        for (int i=0; i<params.length; i++) {
            Class<?> type = params[i];
            ApiField fld = null;
            for (Annotation a: annotations[i]) {
                if (a instanceof ApiField) {
                    if (fld != null) {
                        throw new IllegalArgumentException(i + ". parameter has multiple ApiField annotations: " + method);
                    }
                    fld = (ApiField) a;
                }
            }
            if (type.isAssignableFrom(HttpServletRequest.class)) {
                if (httpRequestIdx >=0) {
                    throw new IllegalArgumentException("only 1 HttpServletRequest possible for method " + method);
                }
                httpRequestIdx = i;
                canHandleBody = true;

            } else if (type.isAssignableFrom(HttpServletResponse.class)) {
                if (httpResponseIdx >=0) {
                    throw new IllegalArgumentException("only 1 HttpServletResponse possible for method " + method);
                }
                httpResponseIdx = i;

            } else if (type.isAssignableFrom(FileUpload.class)) {
                if (fileUploadIdx >=0) {
                    throw new IllegalArgumentException("only 1 FileUpload possible for method " + method);
                }
                sirenBuilder
                        .withType(Action.TYPE_MULTIPART_FORM_DATA)
                        .withMethod(Action.Method.POST);
                fileUploadIdx = i;
                canHandleBody = true;

            } else if (type.isAssignableFrom(JsonObject.class)) {
                if (jsonBodyIdx >=0) {
                    throw new IllegalArgumentException("only 1 JsonObject possible for method " + method);
                }
                sirenBuilder.withType(Action.TYPE_JSON);
                jsonBodyIdx = i;
                canHandleBody = true;

            } else if (type.isAssignableFrom(InputStream.class) && fld == null) {
                if (inputStreamBodyIdx >=0) {
                    throw new IllegalArgumentException("only 1 InputStream body possible for method " + method);
                }
                sirenBuilder.withType(Action.TYPE_APPLICATION_OCTET_STREAM);
                inputStreamBodyIdx = i;
                canHandleBody = true;

            } else if (fld != null) {
                Field f = createField(fld, type);
                sirenBuilder.addField(f);
                fields.put(f.getName(), new FieldInfo(f, type, i));
                if ("file".equals(f.getType())) {
                    sirenBuilder
                            .withType(Action.TYPE_MULTIPART_FORM_DATA)
                            .withMethod(Action.Method.POST);
                }
            }
        }
        if (!sirenBuilder.getFields().isEmpty() && fields.isEmpty() && !canHandleBody) {
            throw new IllegalArgumentException("action fields defined but no parameter can handle them " + method);
        }

        return new ActionInfo(sirenBuilder.build(), method, fields);
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
