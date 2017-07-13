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

import javax.annotation.Nonnull;

import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.registry.PackageTask;

/**
 * {@code PackageTaskImpl}...
 */
public class PackageTaskImpl implements PackageTask {

    private final PackageId id;

    private final Type type;

    private State state = State.NEW;

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

    @Override
    public String toString() {
        return "PackageTaskImpl{" + "id=" + id +
                ", type=" + type +
                ", state=" + state +
                '}';
    }
}