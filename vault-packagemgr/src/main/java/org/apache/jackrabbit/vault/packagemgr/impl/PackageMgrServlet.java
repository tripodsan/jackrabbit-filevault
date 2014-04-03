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
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.LinkBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@SlingServlet(paths = {
        "/system/jackrabbit/filevault"
})
public class PackageMgrServlet extends SlingAllMethodsServlet {

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
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String selfRef = request.getRequestURI();

        String suffix = request.getRequestPathInfo().getSuffix();
        Entity root = null;
        if (suffix == null || suffix.length() == 0) {
            root = new EntityBuilder()
                    .addClass("filevault")
                    .addProperty("version", "3.1.0")
                    .addProperty("api-version", "1.0")
                    .addLink(Rels.SELF, selfRef)
                    .addLink(Rels.VLT_PACKAGES, selfRef + "/packages")
                    .build();
        } else if ("/packages".equals(suffix)) {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            JcrPackageManager mgr = packaging.getPackageManager(session);
            try {
                root = getPackagesEntity(mgr, selfRef);
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
        } else if (suffix.startsWith("/packages/")) {
            
        }

        try {
            SirenJsonWriter out = new SirenJsonWriter(response.getWriter());
            out.write(root);
        } catch (JSONException e) {
            throw new IOException(e);
        }
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
        EntityBuilder builder = new EntityBuilder()
                .addLink(Rels.VLT_PACKAGE_DOWNLOAD, pkgRef + "/" + id.getDownloadName())
                .addProperty("pid", id.toString())
                .addProperty("name", id.getName())
                .addProperty("group", id.getGroup())
                .addProperty("version", id.getVersionString())
                .addProperty("downloadName", id.getDownloadName())
                .addProperty("downloadSize", pkg.getSize())
//                    .addProperty("lastModified", def.getLastModified().getTimeInMillis())
//                    .addProperty("lastModifiedBy", def.getLastModifiedBy())
//                    .addProperty("lastUnpacked", def.getLastUnpacked().getTimeInMillis())
//                    .addProperty("lastUnpackedBy", def.getLastUnpackedBy())
//                    .addProperty("created", def.getCreated().getTimeInMillis())
//                    .addProperty("createdBy", def.getCreatedBy())
                ;
        if (brief) {
            builder.addClass("package-brief")
                   .addLink(Rels.SELF, pkgRef + "?format=brief")
                   .addLink(Rels.VLT_PACKAGE, pkgRef);
        } else {
            builder.addClass("package")
                   .addLink(Rels.SELF, pkgRef)
                   .addLink(Rels.VLT_PACKAGE_BRIEF, pkgRef + "?format=brief");
        }
        return builder.build();
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
    }

}

