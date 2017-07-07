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
package org.apache.jackrabbit.vault.packaging.registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.NoSuchPackageException;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageExistsException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.osgi.annotation.versioning.ProviderType;

/**
 * {@code PackagePersistence}...
 */
@ProviderType
public interface PackageRegistry {

    boolean contains(@Nonnull PackageId id) throws IOException;

    @Nonnull
    Set<PackageId> packages() throws IOException;

    @Nullable
    RegisteredPackage open(@Nonnull PackageId id) throws IOException;

    @Nonnull
    PackageId register(@Nonnull InputStream in, boolean replace) throws IOException, PackageExistsException;

    @Nonnull
    PackageId register(@Nonnull File file, boolean isTempFile, boolean replace) throws IOException, PackageExistsException;

    @Nonnull
    PackageId registerExternal(@Nonnull File file, boolean replace) throws IOException, PackageExistsException;

    void remove(@Nonnull PackageId id) throws IOException, NoSuchPackageException;

    /**
     * Creates a dependency report that lists the resolved and unresolved dependencies.
     * @param id the package id.
     * @param onlyInstalled if {@code true} only installed packages are used for resolution
     * @return the report
     * @throws IOException if an error accessing the repository occurrs
     * @throws NoSuchPackageException if the package with the given {@code id} does not exist.
     */
    @Nonnull
    DependencyReport analyzeDependencies(@Nonnull PackageId id, boolean onlyInstalled) throws IOException, NoSuchPackageException;

    @Nullable
    PackageId resolve(@Nonnull Dependency dependency, boolean onlyInstalled) throws IOException;

    @Nonnull
    PackageId[] usage(@Nonnull PackageId id) throws IOException;

    @Nonnull
    ExecutionPlanBuilder createExecutionPlan();

    @Nonnull
    Map<String, ExecutionPlan> getAsyncExecutionPlans();
}