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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.registry.ExecutionPlan;
import org.apache.jackrabbit.vault.packaging.registry.PackageRegistry;
import org.apache.jackrabbit.vault.packaging.registry.PackageTask;

/**
 * {@code ExecutionPlanImpl}...
 */
public class ExecutionPlanImpl implements ExecutionPlan {

    private List<PackageTask> tasks = new ArrayList<PackageTask>();

    private String id;

    private PackageRegistry registry;

    private ProgressTrackerListener listener;

    public ExecutionPlanImpl(List<PackageTask> tasks) {
        this.tasks.addAll(tasks);
    }

    public List<PackageTask> getTasks() {
        return tasks;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    ExecutionPlanImpl setId(String id) {
        this.id = id;
        return this;
    }

    ExecutionPlanImpl with(PackageRegistry registry) {
        this.registry = registry;
        return this;
    }

    ExecutionPlanImpl with(ProgressTrackerListener listener) {
        this.listener = listener;
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlan execute() throws IOException, PackageException {
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlan executeAsync() throws IOException, PackageException {
        throw new UnsupportedOperationException("async plans are not implemented yet");
    }
}