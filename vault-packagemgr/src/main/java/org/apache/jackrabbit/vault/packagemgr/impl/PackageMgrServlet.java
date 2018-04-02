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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.apache.sling.commons.json.JSONException;
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
                root = new EntityBuilder()
                        .addClass("filevault")
                        .addProperty("version", "3.2.0")
                        .addProperty("api-version", "1.0")
                        .addLink(Rels.SELF, baseRef)
                        .addLink(Rels.REL_VLT_PACKAGES, baseRef + "/packages")
                        .build();
            } else if ("/packages".equals(relPath)) {
                Session session = resolver.adaptTo(Session.class);
                JcrPackageManager mgr = packaging.getPackageManager(session);
                root = getPackagesEntity(mgr, baseRef + "/packages");

            } else if (relPath.startsWith("/packages/")) {
                String format = request.getParameter("format");
                String path = relPath.substring("/packages".length());
                Route r = new Route(path);
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
                } else if ("screenshot".equals(r.getCommand())) {
                    JcrPackageDefinition def = pkg.getDefinition();
                    Node node = def.getNode();
                    if (!node.hasNode("screenshots/" + r.getFile())) {
                        response.sendError(404);
                    } else {
                        node = node.getNode("screenshots/" + r.getFile());
                        if (node.hasProperty("jcr:content/jcr:data")) {
                            sendFile(request, response, node);
                        } else if (node.hasNode("file")) {
                            sendFile(request, response, node.getNode("file"));
                        } else {
                            response.sendError(404);
                        }
                    }
                }
                root = getPackageEntity(pkg, baseRef + "/packages", "brief".equals(format));
            }
            SirenJsonWriter out = new SirenJsonWriter(response.getWriter());
            out.write(root);
        } catch (RepositoryException | JSONException e) {
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

    private Entity getPackagesEntity(JcrPackageManager mgr, String selfRef) throws RepositoryException {
        List<JcrPackage> packages = mgr.listPackages();
        EntityBuilder root = new EntityBuilder()
                .addClass("packages")
                .addProperty("itemCount", packages.size())
                .addLink(Rels.SELF, selfRef);

        for (JcrPackage pkg: packages) {
            root.addEntity(getPackageEntity(pkg, selfRef, true));
        }
        return root.build();
    }

    private Entity getPackageEntity(JcrPackage pkg, String parentRef, boolean brief) throws RepositoryException {
        JcrPackageDefinition  def = pkg.getDefinition();
        PackageId id = def.getId();
        StringBuilder b = new StringBuilder(parentRef).append("/");
        if (id.getGroup().length() > 0) {
            b.append(id.getGroup());
            b.append("/");
        }
        b.append(id.getName());
        if (id.getVersionString().length() > 0) {
            b.append("/").append(id.getVersionString());
        }
        String pkgRef = b.toString();
        EntityBuilder builder = addPackageProperties(new EntityBuilder(), pkg, brief)
                .addLink(Rels.REL_VLT_PACKAGE_DOWNLOAD, pkgRef + "/" + id.getDownloadName());

        if (brief) {
            builder.addClass("package-brief")
                    .addLink(Rels.SELF, pkgRef + "?format=brief")
                    .addLink(Rels.REL_VLT_PACKAGE, pkgRef);
        } else {
            builder.addClass("package")
                    .addLink(Rels.SELF, pkgRef)
                    .addLink(Rels.REL_VLT_PACKAGE_BRIEF, pkgRef + "?format=brief");

            if (def.get("thumbnail.png/jcr:content/jcr:mimeType") != null) {
                builder.addLink(Rels.REL_VLT_THUMBNAIL, pkgRef + "/thumbnail.png");
            }

            if (def.getNode().hasNode("screenshots")) {
                NodeIterator it = def.getNode().getNode("screenshots").getNodes();
                while (it.hasNext()) {
                    builder.addLink(Rels.REL_VLT_SCREENSHOT, pkgRef + "/screenshot/" + it.nextNode().getName());
                }
            }
        }
        return builder.build();
    }

    private EntityBuilder addPackageProperties(EntityBuilder b, JcrPackage pkg, boolean brief) throws RepositoryException {
        JcrPackageDefinition  def = pkg.getDefinition();
        PackageId id = def.getId();
        b.addProperty("pid", id.toString())
                .addProperty("name", id.getName())
                .addProperty("group", id.getGroup())
                .addProperty("version", id.getVersionString())
                .addProperty("downloadName", id.getDownloadName())
                .addProperty("downloadSize", pkg.getSize())
                .addProperty("lastUnpacked", def.getLastUnpacked())
                .addProperty("lastModified", def.getLastModified())
                .addProperty("created", def.getCreated());

        if (brief) {
            return b;
        }
        Node defNode = def.getNode();
        b
                .addProperty("description", def.getDescription())
                .addProperty("buildCount", def.getBuildCount())
                .addProperty("lastModifiedBy", def.getLastModifiedBy())
                .addProperty("lastUnpackedBy", def.getLastUnpackedBy())
                .addProperty("createdBy", def.getCreatedBy())
                .addProperty("hasSnapshot", pkg.getSnapshot() != null)
                .addProperty("needsRewrap", needsRewrap(pkg, id))
                .addProperty("builtWith", def.get("builtWith"))
                .addProperty("testedWith", def.get("testedWith"))
                .addProperty("fixedBugs", def.get("fixedBugs"))
                .addProperty("requiresRoot", def.requiresRoot())
                .addProperty("requiresRestart", def.requiresRestart())
                .addProperty("acHandling",
                        def.getAccessControlHandling() == null
                                ? ""
                                : def.getAccessControlHandling().name().toLowerCase())
                .addProperty("providerName", def.get("providerName"))
                .addProperty("providerUrl", def.get("providerUrl"))
                .addProperty("providerLink", def.get("providerLink"))
                .addProperty("dependencies", defNode, "dependencies")
                .addProperty("replaces", defNode, "replaces")
                .addProperty("workspaceFilter", def.getMetaInf().getFilter());
        return b;
    }

    private boolean needsRewrap(JcrPackage pack, PackageId id) throws RepositoryException {
        String groupPath = id.getGroup().equals("") ? id.getGroup() : "/" + id.getGroup();
        return !pack.getNode().getParent().getPath().equals(PackageId.ETC_PACKAGES + groupPath);
    }

//    private String getThumbnailUrl(Node defNode, String path) throws RepositoryException {
//        long ck = System.currentTimeMillis();
//        try {
//            ck = defNode.getProperty("thumbnail.png/jcr:content/jcr:lastModified").getLong();
//        } catch (PathNotFoundException e) {
//            // ignore
//        }
//        return request.getContextPath() + request.getServletPath() + "/thumbnail.jsp?_charset_=utf-8&path=" + Text.escape(path) + "&ck=" + ck;
//    }
//


    protected static class Route {

        private static final Set<String> commands = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
                "install", "uninstall"
        )));
        private PackageId pkgId;

        private String command;

        private String file;

        protected Route(String path) {
            String[] segs = Text.explode(path, '/');
            // empty
            if (segs.length == 0) {
                return;
            }

            // /name
            if (segs.length == 1) {
                pkgId = new PackageId("", segs[0], "");
                return;
            }

            String last = segs[segs.length-1];
            if ("thumbnail.png".equals(last)) {
                pkgId = createPackageId(segs, 0, segs.length - 1);
                command = "thumbnail";
                file = last;
            } else if (segs.length > 2 && "screenshot".equals(segs[segs.length - 2])) {
                pkgId = createPackageId(segs, 0, segs.length - 2);
                command = "screenshot";
                file = last;
            } else {
                pkgId = createPackageId(segs, 0, segs.length - 1);
                if (last.equals(pkgId.getDownloadName())) {
                    file = last;
                    command = "download";
                } else if (commands.contains(last)) {
                    command = last;
                } else {
                    pkgId = createPackageId(segs, 0, segs.length);
                }
            }
        }

        private static PackageId createPackageId(String[] segs, int from, int to) {
            StringBuilder b = new StringBuilder();
            while (from < to - 2) {
                String seg = segs[from++];
                if (!seg.equals("-")) {
                    b.append("/").append(seg);
                }
            }
            String group = b.toString();
            String name = segs[to-2];
            String version = segs[to-1].equals("-") ? "" : segs[to-1];
            return new PackageId(group, name, version);
        }

        public PackageId getPackageId() {
            return pkgId;
        }

        public String getCommand() {
            return command;
        }

        public String getFile() {
            return file;
        }
    }
}

