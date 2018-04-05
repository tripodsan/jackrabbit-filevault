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

package org.apache.jackrabbit.vault.packagemgr.impl.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.DependencyResolver;
import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageModel extends Base {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(PackageModel.class);

    public static Field FIELD_DESCRIPTION = new FieldBuilder()
            .withName("description")
            .withTitle("Descriptive text of this package.");
    public static Field FIELD_BUILT_WITH = new FieldBuilder()
            .withName("builtWith")
            .withTitle("System that built this package.");
    public static Field FIELD_TESTED_WITH = new FieldBuilder()
            .withName("testedWith")
            .withTitle("System(s) that this package was tested on.");
    public static Field FIELD_FIXED_BUGS = new FieldBuilder()
            .withName("fixedBugs")
            .withTitle("List of fixed bugs; newline separated");
    public static Field FIELD_REQUIRES_ROOT = new FieldBuilder()
            .withName("requiresRoot")
            .withType(Field.Type.CHECKBOX)
            .withTitle("Flag informing that package installation needs admin access (deprecated).");
    public static Field FIELD_REQUIRES_RESTART = new FieldBuilder()
            .withName("requiresRestart")
            .withType(Field.Type.CHECKBOX)
            .withTitle("Flag informing that system needs restart after package installation.");
    public static Field FIELD_ACCESS_CONTROL_HANDLING = new FieldBuilder()
            .withName("acHandling")
            .withTitle("Default access control handling. One of: 'merge', 'merge_preserve', 'overwrite', 'clear', 'ignore'");
    public static Field FIELD_PROVIDER_NAME = new FieldBuilder()
            .withName("providerName")
            .withTitle("Name of the provider; eg company or author");
    public static Field FIELD_PROVIDER_URL = new FieldBuilder()
            .withName("providerUrl")
            .withTitle("URL of the homepage of the provider");
    public static Field FIELD_PROVIDER_LINK = new FieldBuilder()
            .withName("providerLink")
            .withTitle("Link to more information about this package.");
    public static Field FIELD_REPLACES = new FieldBuilder()
            .withName("providerUrl")
            .withTitle("JSON array of package id's of packages that are superseded by this package.");
    public static Field FIELD_WORKSPACE_FILTER = new FieldBuilder()
            .withName("workspaceFilter")
            .withTitle("JSON object or XML of a workspace filter definition.");
    public static Field FIELD_DEPENDENCIES = new FieldBuilder()
            .withName("dependencies")
            .withTitle("JSON array of package dependencies");

    public static String CLASS = "package";

    public static String CLASS_BRIEF = "package-brief";

    private boolean brief;

    private PackageRoute route;

    private JcrPackage pkg;

    private JcrPackageManager mgr;

    private DependencyResolver dependencyResolver;

    public PackageModel withBrief(boolean brief) {
        this.brief = brief;
        return this;
    }

    public PackageModel withPackageManager(JcrPackageManager mgr) {
        this.mgr = mgr;
        return this;
    }

    public PackageModel withPackage(JcrPackage pkg) {
        this.pkg = pkg;
        return this;
    }

    public PackageModel withRoute(PackageRoute route) {
        this.route = route;
        return this;
    }

    public PackageModel withDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
        return this;
    }

    @Override
    public Entity buildEntity() throws IOException {
        try {
            return getPackageEntity();
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    private boolean handleCommand(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ("thumbnail".equals(route.getCommand())) {
                JcrPackageDefinition def = pkg.getDefinition();
                if (def.getNode().hasNode("thumbnail.png")) {
                    Node node = def.getNode().getNode("thumbnail.png");
                    sendFile(request, response, node);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if ("download".equals(route.getCommand())) {
                Node node = pkg.getNode();
                sendFile(request, response, node);
            } else if ("screenshot".equals(route.getCommand())) {
                JcrPackageDefinition def = pkg.getDefinition();
                Node node = def.getNode();
                if (!node.hasNode("screenshots/" + route.getFile())) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    node = node.getNode("screenshots/" + route.getFile());
                    if (node.hasProperty("jcr:content/jcr:data")) {
                        sendFile(request, response, node);
                    } else if (node.hasNode("file")) {
                        sendFile(request, response, node.getNode("file"));
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            } else {
                return false;
            }
            return true;
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (pkg == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!handleCommand(request, response)) {
            super.doGet(request, response);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (pkg == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            mgr.remove(pkg);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (pkg != null) {
            response.sendError(HttpServletResponse.SC_CONFLICT);
            return;
        }

        final PackageId id = route.getPackageId();
        try (JcrPackage pkg = mgr.create(id.getGroup(), id.getName(), id.getVersion().toString())) {
            PackageId newId = pkg.getDefinition().getId();
            if (!newId.equals(id)) {
                log.warn("desired package id {} adjusted to {}", id, newId);
            }
            String location = PackageRoute.getPackageAPIPath(baseHref, newId);
            response.setHeader("Location", location);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    private Entity getPackageEntity() throws RepositoryException {
        JcrPackageDefinition def = pkg.getDefinition();
        PackageId id = def.getId();
        final String pkgRef = PackageRoute.getPackageAPIPath(baseHref, id);
        EntityBuilder builder = addPackageProperties(new EntityBuilder(), def, id)
                .addLink(Rels.REL_VLT_PACKAGE_DOWNLOAD, pkgRef + "/" + id.getDownloadName());

        if (brief) {
            builder.addClass(CLASS_BRIEF)
                    .addLink(Rels.SELF, pkgRef + "?format=brief")
                    .addLink(Rels.REL_VLT_PACKAGE, pkgRef);
        } else {
            builder.addClass(CLASS)
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

        builder.addAction(new ActionBuilder()
                .withName("delete-package")
                .withDELETE()
        );
        return builder.build();
    }

    private EntityBuilder addPackageProperties(EntityBuilder b, JcrPackageDefinition def, PackageId id) throws RepositoryException {
        b.addProperty("pid", id.toString())
                .addProperty("name", id.getName())
                .addProperty("group", id.getGroup())
                .addProperty("version", id.getVersionString())
                .addProperty("downloadName", id.getDownloadName())
                .addProperty("downloadSize", pkg.getSize())
                .addProperty("isInstalled", pkg.isInstalled())
                .addProperty("isModified", def.isModified())
        ;

        if (brief) {
            return b;
        }
        Node defNode = def.getNode();
        b
                .addProperty("description", def.getDescription())
                .addProperty("buildCount", def.getBuildCount())
                .addProperty("lastModified", def.getLastModified())
                .addProperty("lastModifiedBy", def.getLastModifiedBy())
                .addProperty("lastUnpacked", def.getLastUnpacked())
                .addProperty("lastUnpackedBy", def.getLastUnpackedBy())
                .addProperty("created", def.getCreated())
                .addProperty("createdBy", def.getCreatedBy())
                .addProperty("hasSnapshot", pkg.getSnapshot() != null)
                .addProperty("builtWith", def.get("builtWith"))
                .addProperty("testedWith", def.get("testedWith"))
                .addProperty("fixedBugs", def.get("fixedBugs"))
                .addProperty("requiresRoot", def.requiresRoot())
                .addProperty("requiresRestart", def.requiresRestart())
                .addProperty("acHandling",def.getAccessControlHandling())
                .addProperty("providerName", def.get("providerName"))
                .addProperty("providerUrl", def.get("providerUrl"))
                .addProperty("providerLink", def.get("providerLink"))
                .addProperty("replaces", defNode, "replaces")
                .addProperty("workspaceFilter", def.getMetaInf().getFilter());
        addPackageDependencies(b, def);
        return b;
    }

    private void addPackageDependencies(EntityBuilder b, JcrPackageDefinition def) {
        boolean allResolved = true;
        Dependency[] deps = def.getDependencies();
        Map<String, Object> resolved = new HashMap<>();
        for (Dependency d: deps) {
            try {
                String pkgId = dependencyResolver.resolve(d);
                resolved.put(d.toString(), pkgId);
                if (pkgId.isEmpty()) {
                    allResolved = false;
                }
            } catch (RepositoryException e) {
                log.error("unable to resolve dependencies", e);
            }
        }
        b.addProperty("dependencies", resolved);
        b.addProperty("isResolved", allResolved);
    }
}
