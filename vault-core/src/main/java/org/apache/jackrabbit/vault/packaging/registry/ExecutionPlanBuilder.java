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

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Helps to construct an execution plan that can be serialized and given to the packaging backend for execution.
 */
@ProviderType
public interface ExecutionPlanBuilder {

    @Nonnull
    ExecutionPlanBuilder withInstallPackages(@Nonnull ImportOptions opts, @Nonnull PackageId ... id);

    @Nonnull
    ExecutionPlanBuilder withExtractPackages(@Nonnull ImportOptions opts, @Nonnull PackageId ... id);

    @Nonnull
    ExecutionPlanBuilder withUninstallPackages(@Nonnull PackageId... id);

    @Nonnull
    ExecutionPlanBuilder withRemovePackages(@Nonnull PackageId... id);

    @Nonnull
    ExecutionPlanBuilder fromSource(@Nonnull InputStream in);

    @Nonnull
    ExecutionPlanBuilder withSession(@Nonnull Session session);

    @Nonnull
    ExecutionPlan build() throws IOException, PackageException;
}