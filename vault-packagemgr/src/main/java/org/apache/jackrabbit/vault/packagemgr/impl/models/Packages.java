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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.jackrabbit.vault.packagemgr.impl.DependencyResolver;
import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiEntities;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageExistsException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiModel
public class Packages extends Base<Packages> {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(Packages.class);

    public static final String PARAM_PACKAGE = "package";

    public static final String PARAM_REPLACE = "replace";

    private JcrPackageManager pkgMgr;

    private List<JcrPackage> packages;

    public Packages withPackageManager(JcrPackageManager pkgMgr) {
        this.pkgMgr = pkgMgr;
        return this;
    }

    @ApiClass
    public static final String CLASS = "packages";

    @ApiEntities
    public Iterable<PackageModel> entities() throws IOException {
        // todo: better with iteration/streams
        List<JcrPackage> packages = listPackages();
        List<PackageModel> entities = new ArrayList<>(packages.size());
        DependencyResolver depResolver = new DependencyResolver(pkgMgr);
        for (JcrPackage pkg : packages) {
            try {
                entities.add(new PackageModel()
                        .withBaseURI(getBaseURI())
                        .withPackage(pkg)
                        .withDependencyResolver(depResolver)
                        .withBrief(true)
                );
            } catch (RepositoryException e) {
                log.error("Error while including package: {}", pkg);
            }
        }
        return entities;
    }
    
    @ApiProperty
    public long getItemCount() throws IOException {
        return listPackages().size();
    }

//    @ApiAction(
//            method = ApiAction.Method.PUT,
//            name = "create-package",
//            title = "Create new package with JSON payload as initial values",
//            type = ApiAction.TYPE_JSON,
//            href = "/{packageId}"
//    )
//    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        // todo
//    }

    private static class Uploader {

        private File tmpFile;

        private InputStream in;

        private boolean replace;

        public void setInputStream(InputStream in) {
            this.in = in;
        }

        public void setReplace(boolean replace) {
            this.replace = replace;
        }

        public void setTmpFile(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        public PackageId upload(JcrPackageManager mgr) throws IOException, PackageExistsException {
            JcrPackage pkg = null;
            try {
                if (tmpFile == null) {
                    try {
                        pkg = mgr.upload(in, replace, true);
                    } finally {
                        in.close();
                    }
                } else {
                    pkg = mgr.upload(tmpFile, true, replace, null, true);
                }
                return pkg.getDefinition().getId();
            } catch (RepositoryException e) {
                if (e.getCause() instanceof PackageExistsException) {
                    throw (PackageExistsException) e.getCause();
                }
                throw new IOException(e);
            } finally {
                if (pkg != null) {
                    pkg.close();
                }
            }
        }
    }

//    @ApiAction(
//            method = ApiAction.Method.POST,
//            name = "upload-raw-package",
//            type = ApiAction.TYPE_APPLICATION_OCTET_STREAM,
//            title = "Upload package with binary body"
//    )
//    public void doPost2() {
//
//    }

    @ApiAction(
            method = ApiAction.Method.POST,
            name = "upload-package",
            title = "Upload package with multipart formdata.",
            type = ApiAction.TYPE_MULTIPART_FORM_DATA,
            fields = {
                    @ApiField(name = PARAM_PACKAGE, type = ApiField.Type.FILE, title = "Package File"),
                    @ApiField(name = PARAM_REPLACE, type = ApiField.Type.CHECKBOX, title = "Replace existing package")
            }
    )
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // support "raw" binary upload
        Uploader uploader = new Uploader();
        String contentType = request.getContentType();
        if (contentType == null || contentType.isEmpty() || Action.TYPE_APPLICATION_OCTET_STREAM.equals(contentType)) {
            uploader.setInputStream(request.getInputStream());
        } else if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        } else {
            DiskFileItemFactory factory = new DiskFileItemFactory(16 * 1024 * 1024, new File(System.getProperty("java.io.tmpdir")));
            ServletFileUpload upload = new ServletFileUpload(factory);

            FileItem pkgItem = null;
            try {
                for (FileItem item : upload.parseRequest(request)) {
                    if (item.isFormField()) {
                        if (PARAM_REPLACE.equals(item.getFieldName())) {
                            uploader.setReplace(Boolean.valueOf(item.getString()));
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
            File tmpFile = !pkgItem.isInMemory() && pkgItem instanceof DiskFileItem ? ((DiskFileItem) pkgItem).getStoreLocation() : null;
            if (tmpFile == null) {
                uploader.setInputStream(pkgItem.getInputStream());
            } else {
                uploader.setTmpFile(tmpFile);
            }
        }

        try {
            PackageId id = uploader.upload(pkgMgr);
            String location = PackageRoute.getPackageURI(getBaseURI(), id).toString();
            response.setHeader("Location", location);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (PackageExistsException e) {
            String location = PackageRoute.getPackageURI(getBaseURI(), e.getId()).toString();
            response.setHeader("Location", location);
            response.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }

    private List<JcrPackage> listPackages() throws IOException {
        try {
            if (packages == null) {
                packages = pkgMgr.listPackages();
            }
            return packages;
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }
}
