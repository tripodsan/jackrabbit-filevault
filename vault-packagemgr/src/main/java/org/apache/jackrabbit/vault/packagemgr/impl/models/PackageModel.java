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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.packagemgr.impl.DependencyResolver;
import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiModel
public class PackageModel extends Base<PackageModel> {

    public static final String REL_VLT_THUMBNAIL = Filevault.VLT_BASE_URI + "/thumbnail";

    public static final String REL_VLT_SCREENSHOT = Filevault.VLT_BASE_URI + "/screenshot";
    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(PackageModel.class);

    public static Field FIELD_DESCRIPTION = new FieldBuilder()
            .withName("description")
            .withTitle("Descriptive text of this package.")
            .build();
    public static Field FIELD_BUILT_WITH = new FieldBuilder()
            .withName("builtWith")
            .withTitle("System that built this package.")
            .build();
    public static Field FIELD_TESTED_WITH = new FieldBuilder()
            .withName("testedWith")
            .withTitle("System(s) that this package was tested on.")
            .build();
    public static Field FIELD_FIXED_BUGS = new FieldBuilder()
            .withName("fixedBugs")
            .withTitle("List of fixed bugs; newline separated")
            .build();
    public static Field FIELD_REQUIRES_ROOT = new FieldBuilder()
            .withName("requiresRoot")
            .withType(Field.Type.CHECKBOX)
            .withTitle("Flag informing that package installation needs admin access (deprecated).")
            .build();
    public static Field FIELD_REQUIRES_RESTART = new FieldBuilder()
            .withName("requiresRestart")
            .withType(Field.Type.CHECKBOX)
            .withTitle("Flag informing that system needs restart after package installation.")
            .build();
    public static Field FIELD_ACCESS_CONTROL_HANDLING = new FieldBuilder()
            .withName("acHandling")
            .withTitle("Default access control handling. One of: 'merge', 'merge_preserve', 'overwrite', 'clear', 'ignore'")
            .build();
    public static Field FIELD_PROVIDER_NAME = new FieldBuilder()
            .withName("providerName")
            .withTitle("Name of the provider; eg company or author")
            .build();
    public static Field FIELD_PROVIDER_URL = new FieldBuilder()
            .withName("providerUrl")
            .withTitle("URL of the homepage of the provider")
            .build();
    public static Field FIELD_PROVIDER_LINK = new FieldBuilder()
            .withName("providerLink")
            .withTitle("Link to more information about this package.")
            .build();
    public static Field FIELD_REPLACES = new FieldBuilder()
            .withName("providerUrl")
            .withTitle("JSON array of package id's of packages that are superseded by this package.")
            .build();
    public static Field FIELD_WORKSPACE_FILTER = new FieldBuilder()
            .withName("workspaceFilter")
            .withTitle("JSON object or XML of a workspace filter definition.")
            .build();
    public static Field FIELD_DEPENDENCIES = new FieldBuilder()
            .withName("dependencies")
            .withTitle("JSON array of package dependencies")
            .build();

    public static String CLASS = "package";

    public static String CLASS_BRIEF = "package-brief";

    private boolean brief;

    private PackageRoute route;

    private JcrPackage pkg;

    private JcrPackageManager mgr;

    private DependencyResolver dependencyResolver;

    private PackageId id;

    private JcrPackageDefinition def;

    private Map<String, String> resolvedDependencies;

    private boolean isResolved;

    public PackageModel withBrief(boolean brief) {
        this.brief = brief;
        return this;
    }

    public PackageModel withPackageManager(JcrPackageManager mgr) {
        this.mgr = mgr;
        return this;
    }

    public PackageModel withPackage(JcrPackage pkg) throws RepositoryException {
        this.pkg = pkg;
        this.def = pkg.getDefinition();
        this.id = def.getId();
        withRelPath(PackageRoute.getPackageRelPath(id));
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

//    //    @Override
//    public Entity _buildEntity() throws IOException {
//        try {
//            return getPackageEntity();
//        } catch (RepositoryException e) {
//            throw new IOException(e);
//        }
//    }

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

    @ApiAction(
            method = ApiAction.Method.DELETE,
            name = "delete-package"
    )
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
            String location = PackageRoute.getPackageURI(getBaseURI(), newId).toString();
            response.setHeader("Location", location);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

//    private Entity getPackageEntity() throws RepositoryException {
//        JcrPackageDefinition def = pkg.getDefinition();
//        PackageId id = def.getId();
//        final String pkgRef = PackageRoute.getPackageRelPath(baseHref, id);
//        EntityBuilder builder = addPackageProperties(new EntityBuilder(), def, id)
//                .addLink(Rels.REL_VLT_PACKAGE_DOWNLOAD, pkgRef + "/" + id.getDownloadName());
//
//        if (brief) {
//            builder.addClass(CLASS_BRIEF)
//                    .addLink(Rels.SELF, pkgRef + "?format=brief")
//                    .addLink(Rels.REL_VLT_PACKAGE, pkgRef);
//        } else {
//            builder.addClass(CLASS)
//                    .addLink(Rels.SELF, pkgRef)
//                    .addLink(Rels.REL_VLT_PACKAGE_BRIEF, pkgRef + "?format=brief");
//
//            if (def.get("thumbnail.png/jcr:content/jcr:mimeType") != null) {
//                builder.addLink(Rels.REL_VLT_THUMBNAIL, pkgRef + "/thumbnail.png");
//            }
//
//            if (def.getNode().hasNode("screenshots")) {
//                NodeIterator it = def.getNode().getNode("screenshots").getNodes();
//                while (it.hasNext()) {
//                    builder.addLink(Rels.REL_VLT_SCREENSHOT, pkgRef + "/screenshot/" + it.nextNode().getName());
//                }
//            }
//        }
//
//        builder.addAction(new ActionBuilder()
//                .withName("delete-package")
//                .withMethod(Action.Method.DELETE)
//                .build()
//        );
//        return builder.build();
//    }


    @ApiClass
    public String clazz() {
        return brief ? CLASS_BRIEF : CLASS;
    }

    @ApiLink(REL_VLT_THUMBNAIL)
    public String linkThumbnail() {
        if (def.get("thumbnail.png/jcr:content/jcr:mimeType") != null) {
            return "/thumbnail.png";
        }
        return null;
    }

    @ApiLink(REL_VLT_SCREENSHOT)
    public List<String> linkScreenshots() {
        try {
            if (brief || !def.getNode().hasNode("screenshots")) {
                return null;
            }
            List<String> links = new LinkedList<>();
            NodeIterator it = def.getNode().getNode("screenshots").getNodes();
            while (it.hasNext()) {
                links.add("/screenshot/" + it.nextNode().getName());
            }
            return links;
        } catch (RepositoryException e) {
            return null;
        }
    }

    @ApiProperty
    public String getPid() {
        return id.toString();
    }

    @ApiProperty
    public String getGroup() {
        return id.getGroup();
    }

    @ApiProperty
    public String getVersion() {
        return id.getVersionString();
    }

    @ApiProperty
    public String getDownloadName() {
        return id.getDownloadName();
    }

    @ApiProperty
    public long getDownloadSize() {
        return pkg.getSize();
    }

    @ApiProperty
    public Boolean getIsInstalled() {
        try {
            return pkg.isInstalled();
        } catch (RepositoryException e) {
            return false;
        }
    }

    @ApiProperty
    public boolean getIsModified() {
        return def.isModified();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getDescription() {
        return def.getDescription();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public long getBuildCount() {
        return def.getBuildCount();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public Calendar getLastModified() {
        return def.getLastModified();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getLastModifiedBy() {
        return def.getLastModifiedBy();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public Calendar getLastUnpacked() {
        return def.getLastUnpacked();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getLastUnpackedBy() {
        return def.getLastUnpackedBy();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public Calendar getCreated() {
        return def.getCreated();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getCreatedBy() {
        return def.getCreatedBy();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public boolean getHasSnapshot() {
        try {
            return pkg.getSnapshot() != null;
        } catch (RepositoryException e) {
            return false;
        }
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getBuiltWith() {
        return def.get("builtWith");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getTestedWith() {
        return def.get("testedWith");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getFixedBugs() {
        return def.get("fixedBugs");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public boolean getRequiresRoot() {
        return def.requiresRoot();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public boolean getRequiresRestart() {
        return def.requiresRestart();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public AccessControlHandling getAcHandling() {
        return def.getAccessControlHandling();
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getProviderName() {
        return def.get("providerName");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getProviderUrl() {
        return def.get("providerUrl");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public String getProviderLink() {
        return def.get("providerLink");
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public Object getReplaces() {
        try {
            return ReflectionUtils.jcrPropertyToObject(def.getNode(), "replaces");
        } catch (RepositoryException e) {
            return null;
        }
    };

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public Map<String, String> getDependencies() {
        if (resolvedDependencies == null) {
            resolveDependencies();
        }
        return resolvedDependencies;
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public boolean getIsResolved() {
        if (resolvedDependencies == null) {
            resolveDependencies();
        }
        return isResolved;
    }

    @ApiProperty(context = ApiProperty.Context.ENTITY)
    public WorkspaceFilter getWorkspaceFilter() {
        try {
            return def.getMetaInf().getFilter();
        } catch (RepositoryException e) {
            return null;
        }
    }


//    private EntityBuilder addPackageProperties(EntityBuilder b, JcrPackageDefinition def, PackageId id) throws RepositoryException {
//        b.addProperty("pid", id.toString())
//                .addProperty("name", id.getName())
//                .addProperty("group", id.getGroup())
//                .addProperty("version", id.getVersionString())
//                .addProperty("downloadName", id.getDownloadName())
//                .addProperty("downloadSize", pkg.getSize())
//                .addProperty("isInstalled", pkg.isInstalled())
//                .addProperty("isModified", def.isModified())
//        ;
//
//        if (brief) {
//            return b;
//        }
//        Node defNode = def.getNode();
//        b
//                .addProperty("description", def.getDescription())
//                .addProperty("buildCount", def.getBuildCount())
//                .addProperty("lastModified", def.getLastModified())
//                .addProperty("lastModifiedBy", def.getLastModifiedBy())
//                .addProperty("lastUnpacked", def.getLastUnpacked())
//                .addProperty("lastUnpackedBy", def.getLastUnpackedBy())
//                .addProperty("created", def.getCreated())
//                .addProperty("createdBy", def.getCreatedBy())
//                .addProperty("hasSnapshot", pkg.getSnapshot() != null)
//                .addProperty("builtWith", def.get("builtWith"))
//                .addProperty("testedWith", def.get("testedWith"))
//                .addProperty("fixedBugs", def.get("fixedBugs"))
//                .addProperty("requiresRoot", def.requiresRoot())
//                .addProperty("requiresRestart", def.requiresRestart())
//                .addProperty("acHandling", def.getAccessControlHandling())
//                .addProperty("providerName", def.get("providerName"))
//                .addProperty("providerUrl", def.get("providerUrl"))
//                .addProperty("providerLink", def.get("providerLink"))
//                .addProperty("replaces", defNode, "replaces")
//                .addProperty("workspaceFilter", def.getMetaInf().getFilter());
//        addPackageDependencies(b, def);
//        return b;
//    }
//
    private void resolveDependencies() {
        isResolved = true;
        Dependency[] deps = def.getDependencies();
        resolvedDependencies = new HashMap<>();
        for (Dependency d : deps) {
            try {
                String pkgId = dependencyResolver.resolve(d);
                resolvedDependencies.put(d.toString(), pkgId);
                if (pkgId.isEmpty()) {
                    isResolved = false;
                }
            } catch (RepositoryException e) {
                log.error("unable to resolve dependencies", e);
            }
        }
    }

//    private void addPackageDependencies(EntityBuilder b, JcrPackageDefinition def) {
//        if (resolvedDependencies == null) {
//            resolveDependencies();
//        }
//        b.addProperty("dependencies", resolvedDependencies);
//        b.addProperty("isResolved", isResolved);
//    }
}
