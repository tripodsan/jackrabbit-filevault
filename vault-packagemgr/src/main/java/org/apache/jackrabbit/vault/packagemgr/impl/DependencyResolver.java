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

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;

public class DependencyResolver {

    private final JcrPackageManager mgr;

    private final Map<Dependency, String> resolvedDependencies = new HashMap<>();

    public DependencyResolver(JcrPackageManager mgr) {
        this.mgr = mgr;
    }

    public String resolve(Dependency dep) throws RepositoryException {
        String pkgId = resolvedDependencies.get(dep);
        if (pkgId == null) {
            PackageId id = mgr.resolve(dep, false);
            pkgId = id == null ? "": id.toString();
            resolvedDependencies.put(dep, pkgId);
        }
        return pkgId;
    }
}
