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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;

public class ActionInfoContext {

    private final Action sirenAction;

    private final Method method;

    private final Map<String, ParameterInfo> fields;

    private final String name;

    public ActionInfoContext(Action sirenAction, Method method, Map<String, ParameterInfo> fields) {
        this.sirenAction = sirenAction;
        this.method = method;
        this.fields = fields;
    }

    public Action getSirenAction() {
        return sirenAction;
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Map<String, ParameterInfo> getFields() {
        return fields;
    }

    public void execute(Object resource, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            method.invoke(resource, new ParameterBinding(req, resp).getArguments());
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new IOException(t);
        }

    }

    private class ParameterBinding {

        private final HttpServletRequest req;

        private final HttpServletResponse resp;

        private FileUpload upload;

        private ParameterBinding(HttpServletRequest req, HttpServletResponse resp) {
            this.req = req;
            this.resp = resp;
        }

        private Object[] getArguments() throws IOException {
            Object[] args = new Object[method.getParameterTypes().length];
            for (ParameterInfo param: fields.values()) {
                final String name = param.getName();
                if (ParameterInfo.TYPE_REQUEST.equals(name)) {
                    args[param.getIdx()] = req;
                } else if (ParameterInfo.TYPE_RESPONSE.equals(name)) {
                    args[param.getIdx()] = resp;
                } else if (ParameterInfo.TYPE_UPLOAD.equals(name)) {
                    args[param.getIdx()] = getFileUpload();
                } else if (ParameterInfo.TYPE_STREAM_BODY.equals(name)) {
                    args[param.getIdx()] = req.getInputStream();
                } else if (ParameterInfo.TYPE_JSON_BODY.equals(name)) {
                    args[param.getIdx()] = getJsonBody();
                } else {
                    throw new UnsupportedOperationException("unable to handle parameter " + param);
                }
            }
            return args;
        }

        private FileUpload getFileUpload() {
            if (upload == null) {
                upload = new ServletFileUpload(
                        new DiskFileItemFactory(16 * 1024 * 1024, new File(System.getProperty("java.io.tmpdir")))
                );
            }
            return upload;
        }

        private JsonObject getJsonBody() {
            throw new UnsupportedOperationException("JsonObject body not supported yet");
        }
    }
}
