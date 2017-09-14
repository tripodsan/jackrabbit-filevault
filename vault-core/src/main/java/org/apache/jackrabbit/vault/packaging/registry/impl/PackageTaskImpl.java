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
package org.apache.jackrabbit.vault.packaging.registry.impl;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.DependencyHandling;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.jackrabbit.vault.packaging.impl.JcrPackageImpl;
import org.apache.jackrabbit.vault.packaging.registry.PackageTask;
import org.apache.jackrabbit.vault.packaging.registry.RegisteredPackage;

/**
 * {@code PackageTaskImpl}...
 */
public class PackageTaskImpl implements PackageTask {

    final static PackageTaskImpl MARKER = new PackageTaskImpl(new PackageId("", "" ,""), Type.INSTALL);

    private final PackageId id;

    private final Type type;

    private State state = State.NEW;

    private Throwable error;

    public PackageTaskImpl(@Nonnull PackageId id, @Nonnull Type type) {
        this.id = id;
        this.type = type;
    }

    @Nonnull
    @Override
    public PackageId getPackageId() {
        return id;
    }

    @Nonnull
    @Override
    public Type getType() {
        return type;
    }

    @Nonnull
    @Override
    public State getState() {
        return state;
    }

    @Nullable
    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "PackageTaskImpl{" + "id=" + id +
                ", type=" + type +
                ", state=" + state +
                '}';
    }

    void execute(ExecutionPlanImpl executionPlan) {
        if (state != State.NEW) {
            return;
        }
        state = State.RUNNING;
        try {
            switch (type) {
                case INSTALL:
                    doInstall(executionPlan);
                    break;
                case UNINSTALL:
                    doUninstall(executionPlan);
                    break;
                case REMOVE:
                    doRemove(executionPlan);
                    break;
                case EXTRACT:
                    doExtract(executionPlan);
                    break;
            }
            state = State.COMPLETED;
        } catch (Exception e) {
            error  = e;
            state = State.ERROR;
        }
    }

    private void doExtract(ExecutionPlanImpl plan) {
        throw new UnsupportedOperationException();
    }

    private void doRemove(ExecutionPlanImpl plan) {
        throw new UnsupportedOperationException();
    }

    private void doUninstall(ExecutionPlanImpl plan) {
        throw new UnsupportedOperationException();
    }

    private void doInstall(ExecutionPlanImpl plan) throws IOException, PackageException {
        ImportOptions opts = new ImportOptions();
        opts.setListener(plan.getListener());
        // execution plan resolution already has resolved all dependencies, so there is no need to use best effort here.
        opts.setDependencyHandling(DependencyHandling.STRICT);

        try (RegisteredPackage pkg = plan.getRegistry().open(id)) {
            if (!(pkg instanceof JcrRegisteredPackage)) {
                throw new PackageException("non jcr packages not supported yet");
            }
            try (JcrPackage jcrPkg = ((JcrRegisteredPackage) pkg).getJcrPackage()){
                jcrPkg.install(opts);
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
        }
    }


}