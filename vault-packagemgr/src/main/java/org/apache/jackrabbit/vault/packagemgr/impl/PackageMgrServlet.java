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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ResourceResolver resolver = (ResourceResolver) request.getAttribute(AuthenticationSupport.REQUEST_ATTRIBUTE_RESOLVER);
        if (resolver == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        try {
            String relPath = request.getPathInfo();
            if (relPath == null || "/".equals(relPath)) {
                relPath = "";
            }
            String baseRef = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath();
            Entity root = null;
            if (relPath.length() == 0) {
                root = new Filevault()
                        .withBaseHref(baseRef)
                        .buildEntity();

            } else if ("/packages".equals(relPath)) {
                root = new Packages()
                        .withPackageManager(packaging.getPackageManager(resolver.adaptTo(Session.class)))
                        .withBaseHref(baseRef)
                        .buildEntity();

            } else if (relPath.startsWith("/packages/")) {
                String format = request.getParameter("format");
                String path = relPath.substring("/packages".length());
                PackageRoute r = new PackageRoute(path);
                Session session = resolver.adaptTo(Session.class);
                JcrPackageManager mgr = packaging.getPackageManager(session);
                JcrPackage pkg = mgr.open(r.getPackageId());
                if (pkg == null) {
                    response.sendError(404);
                    return;
                }
                if ("thumbnail".equals(r.getCommand())) {
                    JcrPackageDefinition def = pkg.getDefinition();
                    if (def.getNode().hasNode("thumbnail.png")) {
                        Node node = def.getNode().getNode("thumbnail.png");
                        sendFile(request, response, node);
                    } else {
                        response.sendError(404);
                    }
                    return;
                } else if ("download".equals(r.getCommand())) {
                    Node node = pkg.getNode();
                    sendFile(request, response, node);
                    return;
                } else if ("screenshot".equals(r.getCommand())) {
                    JcrPackageDefinition def = pkg.getDefinition();
                    Node node = def.getNode();
                    if (!node.hasNode("screenshots/" + r.getFile())) {
                        response.sendError(404);
                        return;
                    } else {
                        node = node.getNode("screenshots/" + r.getFile());
                        if (node.hasProperty("jcr:content/jcr:data")) {
                            sendFile(request, response, node);
                        } else if (node.hasNode("file")) {
                            sendFile(request, response, node.getNode("file"));
                        } else {
                            response.sendError(404);
                        }
                        return;
                    }
                }
                root = new PackageModel()
                        .withPackage(pkg)
                        .withBrief("brief".equals(format))
                        .withBaseHref(baseRef)
                        .buildEntity();
            }

            try (SirenJsonWriter out = new SirenJsonWriter(response.getWriter())) {
                out.write(root);
            }
        } catch (RepositoryException | JsonException e) {
            throw new IOException(e);
        }
    }

    private void sendFile(HttpServletRequest request, HttpServletResponse response, Node file)
            throws IOException, RepositoryException {
        Binary bin = file.getProperty("jcr:content/jcr:data").getBinary();
        response.setContentType(file.getProperty("jcr:content/jcr:mimeType").getString());
        response.setContentLength((int) bin.getSize());
        IOUtils.copy(bin.getStream(), response.getOutputStream());
        bin.dispose();
    }

}

