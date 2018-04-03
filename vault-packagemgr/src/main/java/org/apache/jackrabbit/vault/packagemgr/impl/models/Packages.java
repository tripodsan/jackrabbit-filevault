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
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;

public class Packages extends Base {

    public static String CLASS = "packages";

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
                .withName("create-package")
                .withType(Action.TYPE_MULTIPART_FORM_DATA)
                .withPOST()
                .addField(new FieldBuilder()
                    .withName("package")
                    .withType("file"))
        );

        return builder.build();
    }

    private List<JcrPackage> listPackages() throws IOException {
        try {
            return pkgMgr.listPackages();
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }
}
