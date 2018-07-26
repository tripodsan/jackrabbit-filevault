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

package org.apache.jackrabbit.vault.packaging.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.jackrabbit.vault.fs.config.ConfigurationException;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.ScopeTracker;
import org.apache.jackrabbit.vault.packaging.ScopedWorkspaceFilter;
import org.apache.jackrabbit.vault.packaging.impl.JcrPackageManagerImpl;
import org.apache.jackrabbit.vault.packaging.registry.impl.JcrPackageRegistry;
import org.apache.tika.io.IOUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import sun.reflect.generics.scope.Scope;

import static org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.PN_DEPENDENCIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@code TestPackageInstall}...
 */
public class TestPackageScopedInstall extends IntegrationTestBase {

    /**
     * Installs a package that contains mixed content
     */
    @Test
    public void testMixedContent() throws RepositoryException, IOException, PackageException {
        JcrPackage pack = packMgr.upload(getStream("testpackages/mixed_package.zip"), false);
        assertNotNull(pack);

        // just extract - no snapshots
        ScopeTracker tracker = ScopeTracker.createApplicationScoped(null);
        ImportOptions opts = getDefaultOptions();
        opts.setListener(tracker);
        pack.extract(opts);
        assertNodeExists("/tmp/foo");
        assertNodeExists("/libs/foo");
    }

    /**
     * Installs a package that contains mixed content but filtered for apps
     */
    @Test
    public void testApplication() throws RepositoryException, IOException, PackageException {
        JcrPackage pack = packMgr.upload(getStream("testpackages/mixed_package.zip"), false);
        assertNotNull(pack);

        // just extract - no snapshots
        ScopedWorkspaceFilter filter = ScopedWorkspaceFilter.createApplicationScoped(
                (DefaultWorkspaceFilter) pack.getDefinition().getMetaInf().getFilter());

        ScopeTracker tracker = ScopeTracker.createApplicationScoped(null);
        ImportOptions opts = getDefaultOptions();
        opts.setFilter(filter);
        opts.setListener(tracker);
        pack.extract(opts);
        assertNodeMissing("/tmp/foo");
        assertNodeExists("/libs/foo");
    }

    /**
     * Installs a package that contains mixed content but filtered for content
     */
    @Test
    public void testContent() throws RepositoryException, IOException, PackageException {
        JcrPackage pack = packMgr.upload(getStream("testpackages/mixed_package.zip"), false);
        assertNotNull(pack);

        // just extract - no snapshots
        ScopedWorkspaceFilter filter = ScopedWorkspaceFilter.createContentScoped((
                DefaultWorkspaceFilter) pack.getDefinition().getMetaInf().getFilter());
        ImportOptions opts = getDefaultOptions();
        opts.setFilter(filter);
        pack.extract(opts);
        assertNodeExists("/tmp/foo");
        assertNodeMissing("/libs/foo");
    }


}