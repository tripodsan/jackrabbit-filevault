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
import java.net.URI;
import java.net.URISyntaxException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.json.JsonException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.models.Base;
import org.apache.jackrabbit.vault.packagemgr.impl.models.Filevault;
import org.apache.jackrabbit.vault.packagemgr.impl.models.PackageModel;
import org.apache.jackrabbit.vault.packagemgr.impl.models.Packages;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfoLoader;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.ResourceContext;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
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

    /**
     * default logger
     */
    protected final Logger log = LoggerFactory.getLogger(PackageMgrServlet.class);

    @Reference
    private Packaging packaging;

    private final ModelInfoLoader loader = new ModelInfoLoader();

    private Base resolve(HttpServletRequest request, HttpServletResponse response)
            throws IOException, RepositoryException, URISyntaxException {
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
        URI baseRef = new URI(request.getScheme() + "://" + request.getHeader("host") + request.getContextPath());

        if (relPath.length() == 0) {
            return new Filevault()
                    .withBaseURI(baseRef);

        } else if ("/packages".equals(relPath)) {
            return new Packages()
                    .withPackageManager(mgr)
                    .withBaseURI(baseRef)
                    .withRelPath("/packages");

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
                    .withBaseURI(baseRef);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Base model = resolve(req, resp);
            if (model == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ModelInfo info = loader.load(model.getClass());
            ResourceContext ctx = new ResourceContext()
                    .withInfoLoader(loader)
                    .withInfo(info)
                    .withBaseURI(model.getBaseURI())
                    .withSelfURI(model.getSelfURI())
                    .withModel(model);

            ctx.service(req, resp);
        } catch (RepositoryException | JsonException | URISyntaxException | IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

}

