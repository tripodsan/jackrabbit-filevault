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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ActionInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ParameterInfo;

public class ActionContext {

    private ActionInfo info;

    private ModelInfo model;

    private ResourceContext context;


    private Object resource;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private FileItem[] form;

    public ActionContext withContext(ResourceContext context) {
        this.context = context;
        return this;
    }

    public ActionContext withActionInfo(ActionInfo info) {
        this.info = info;
        return this;
    }

    public ActionContext withModelInfo(ModelInfo model) {
        this.model = model;
        return this;
    }

    public ActionContext withResource(Object resource) {
        this.resource = resource;
        return this;
    }

    public ActionContext withRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public ActionContext withResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    public void execute() throws IOException {
        try {
            info.getMethod().invoke(resource, getArguments());
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new IOException(t);
        }
    }

    private Object[] getArguments() throws IOException {
        Object[] args = new Object[info.getMethod().getParameterTypes().length];
        for (ParameterInfo param: info.getFields().values()) {
            final String name = param.getName();
            if (ParameterInfo.TYPE_CONTEXT.equals(name)) {
                args[param.getIdx()] = context;
            } else if (ParameterInfo.TYPE_REQUEST.equals(name)) {
                args[param.getIdx()] = request;
            } else if (ParameterInfo.TYPE_RESPONSE.equals(name)) {
                args[param.getIdx()] = response;
            } else if (ParameterInfo.TYPE_FORM.equals(name)) {
                args[param.getIdx()] = getFileUpload();
            } else if (ParameterInfo.TYPE_STREAM_BODY.equals(name)) {
                args[param.getIdx()] = request.getInputStream();
            } else if (ParameterInfo.TYPE_JSON_BODY.equals(name)) {
                args[param.getIdx()] = getJsonBody();
            } else {
                throw new UnsupportedOperationException("unable to handle parameter " + param);
            }
        }
        return args;
    }

    private FileItem[] getFileUpload() throws IOException {
        try {
            if (form == null) {
                ServletFileUpload upload = new ServletFileUpload(
                        new DiskFileItemFactory(16 * 1024 * 1024, new File(System.getProperty("java.io.tmpdir")))
                );
                List<FileItem> items = upload.parseRequest(request);
                form = items.toArray(new FileItem[items.size()]);
            }
            return form;
        } catch (FileUploadException e) {
            throw new IOException(e);
        }
    }

    private JsonObject getJsonBody() {
        throw new UnsupportedOperationException("JsonObject body not supported yet");
    }
}
