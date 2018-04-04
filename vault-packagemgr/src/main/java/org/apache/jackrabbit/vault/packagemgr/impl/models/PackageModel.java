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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;

public class PackageModel extends Base {

    public static String CLASS = "package";

    public static String CLASS_BRIEF = "package-brief";

    private boolean brief;

    private PackageRoute route;

    private JcrPackage pkg;

    private JcrPackageManager mgr;

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
        if (!handleCommand(request, response)) {
            super.doGet(request, response);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            mgr.remove(pkg);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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

}
