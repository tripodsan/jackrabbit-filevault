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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageExistsException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Packages extends Base {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(Packages.class);

    public static final String CLASS = "packages";

    public static final String ACTION_CREATE_PACKAGE = "create-package";
    public static final String PARAM_PACKAGE = "package";
    public static final String PARAM_REPLACE = "replace";


    private JcrPackageManager pkgMgr;

    public Packages withPackageManager(JcrPackageManager pkgMgr) {
        this.pkgMgr = pkgMgr;
        return this;
    }

    @Override
    public Entity buildEntity() throws IOException {
        List<JcrPackage> packages = listPackages();
        EntityBuilder builder = new EntityBuilder()
                .addClass(CLASS)
                .addProperty("itemCount", packages.size())
                .addLink(Rels.SELF, baseHref + "/packages");

        for (JcrPackage pkg: packages) {
            builder.addEntity(new PackageModel()
                    .withPackage(pkg)
                    .withBrief(true)
                    .withBaseHref(baseHref)
                    .buildEntity()
            );
        }
        builder.addAction(new ActionBuilder()
                .withName(ACTION_CREATE_PACKAGE)
                .withType(Action.TYPE_MULTIPART_FORM_DATA)
                .withPOST()
                .withHref(baseHref + "/packages")
                .addField(new FieldBuilder()
                    .withName(PARAM_PACKAGE)
                    .withTitle("Package File")
                    .withType("file"))
                .addField(new FieldBuilder()
                    .withName(PARAM_REPLACE)
                    .withTitle("Replace existing package")
                    .withType("text"))
        );

        return builder.build();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            // upload as raw: TODO
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory(16*1024*1024, new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);

        FileItem pkgItem = null;
        boolean forceUpload = false;
        try {
            for (FileItem item: upload.parseRequest(request)) {
                if (item.isFormField()) {
                    if (PARAM_REPLACE.equals(item.getFieldName())) {
                        forceUpload = Boolean.valueOf(item.getString());
                    }
                } else {
                    if (PARAM_PACKAGE.equals(item.getFieldName())) {
                        pkgItem = item;
                    }
                }
            }
        } catch (FileUploadException e) {
            throw new IOException(e);
        }

        if (pkgItem == null) {
            log.error("create-package is missing 'package' parameter.");
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        // check for file
        File tmpFile = !pkgItem.isInMemory() && pkgItem instanceof DiskFileItem ? ((DiskFileItem) pkgItem).getStoreLocation() : null;
        PackageId id;
        try {
            JcrPackage pkg;
            if (tmpFile == null) {
                try (InputStream in = pkgItem.getInputStream()) {
                    pkg = pkgMgr.upload(in, forceUpload, true);
                }
            } else {
                pkg = pkgMgr.upload(tmpFile, true, forceUpload, null, true);
            }
            id = pkg.getDefinition().getId();
            String location = PackageRoute.getPackageAPIPath(baseHref, id);
            response.setHeader("Location", location);
            response.setStatus(HttpServletResponse.SC_CREATED);
            pkg.close();
        } catch (ItemExistsException e) {
            if (e.getCause() instanceof PackageExistsException) {
                PackageId existingId = ((PackageExistsException) e.getCause()).getId();
                String location = PackageRoute.getPackageAPIPath(baseHref, existingId);
                response.setHeader("Location", location);
            }
            response.sendError(HttpServletResponse.SC_CONFLICT);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }



    }

    private List<JcrPackage> listPackages() throws IOException {
        try {
            return pkgMgr.listPackages();
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }
}
