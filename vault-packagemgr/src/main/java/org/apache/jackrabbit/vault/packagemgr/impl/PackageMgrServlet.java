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

package org.apache.jackrabbit.vault.packagemgr.impl;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.json.JsonException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.models.Base;
import org.apache.jackrabbit.vault.packagemgr.impl.models.Filevault;
import org.apache.jackrabbit.vault.packagemgr.impl.models.PackageModel;
import org.apache.jackrabbit.vault.packagemgr.impl.models.Packages;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                "service.vendor=Apache Software Foundation",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + HttpContext.CONTEXT_NAME + ")"
        }
)
public class PackageMgrServlet extends HttpServlet {

    private static final long serialVersionUID = -4571680968447024900L;
    public static final String PARAM_SRC = "src";
    public static final String PARAM_DST = "dst";
    public static final String PARAM_ID = "id";
    public static final String PARAM_BATCHSIZE = "batchsize";
    public static final String PARAM_CMD = "cmd";
    public static final String PARAM_RECURSIVE = "recursive";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_UPDATE = "update";
    public static final String PARAM_NO_ORDERING = "noOrdering";
    public static final String PARAM_ONLY_NEWER = "onlyNewer";
    public static final String PARAM_THROTTLE = "throttle";
    public static final String PARAM_EXCLUDES = "excludes";
    public static final String PARAM_RESUME_FROM = "resumeFrom";

    /**
     * default logger
     */
    protected final Logger log = LoggerFactory.getLogger(PackageMgrServlet.class);

    @Reference
    private Packaging packaging;

    private Base resolve(HttpServletRequest request, HttpServletResponse response)
            throws IOException, RepositoryException {
        final ResourceResolver resolver = (ResourceResolver) request.getAttribute(AuthenticationSupport.REQUEST_ATTRIBUTE_RESOLVER);
        if (resolver == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        final JcrPackageManager mgr = packaging.getPackageManager(resolver.adaptTo(Session.class));

        String relPath = request.getPathInfo();
        if (relPath == null || "/".equals(relPath)) {
            relPath = "";
        }
        String baseRef = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath();

        if (relPath.length() == 0) {
            return new Filevault()
                    .withBaseHref(baseRef);

        } else if ("/packages".equals(relPath)) {
            return new Packages()
                    .withPackageManager(mgr)
                    .withBaseHref(baseRef);

        } else if (relPath.startsWith("/packages/")) {
            String format = request.getParameter("format");
            String path = relPath.substring("/packages".length());
            PackageRoute r = new PackageRoute(path);
            JcrPackage pkg = mgr.open(r.getPackageId());
            return new PackageModel()
                    .withPackageManager(mgr)
                    .withDependencyResolver(new DependencyResolver(mgr))
                    .withPackage(pkg)
                    .withBrief("brief".equals(format))
                    .withRoute(r)
                    .withBaseHref(baseRef);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Base model = resolve(request, response);
            if (model == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            model.doGet(request, response);
        } catch (RepositoryException | JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Base model = resolve(request, response);
            if (model == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            model.doPost(request, response);
        } catch (RepositoryException | JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Base model = resolve(request, response);
            if (model == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            model.doPut(request, response);
        } catch (RepositoryException | JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Base model = resolve(request, response);
            if (model == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            model.doDelete(request, response);
        } catch (RepositoryException | JsonException e) {
            throw new IOException(e);
        }
    }
}
