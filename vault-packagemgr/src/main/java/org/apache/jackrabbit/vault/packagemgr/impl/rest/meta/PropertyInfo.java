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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.meta;

import java.lang.reflect.Member;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;

public class PropertyInfo {

    private final String name;

    private final Member member;

    private final boolean flatten;

    private final String value;

    private final String[] context;

    public PropertyInfo(String name, Member member, boolean flatten, String value, String[] context) {
        this.name = name;
        this.member = member;
        this.flatten = flatten;
        this.value = value;
        this.context = context;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public String getName() {
        return name;
    }

    public Object getValue(Object resource) {
        return ReflectionUtils.getValue(resource, member);
    }

    public boolean isActive(Set<String> classes, String pseudoClass) {
        if (context.length == 0 || classes.isEmpty()) {
            return true;
        }
        for (String ctx: context) {
            if (classes.contains(ctx) || ctx.equals(pseudoClass)) {
                return true;
            }
        }
        return false;
    }
}
