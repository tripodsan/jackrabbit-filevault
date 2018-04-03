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
package org.apache.jackrabbit.vault.packagemgr.impl.siren.builder;

import java.util.Set;
import java.util.TreeSet;

public class BaseBuilder {

    private TreeSet<String> classes = new TreeSet<String>();

    private String name;

    private String type;

    private String title;

    public BaseBuilder addClass(String className) {
        classes.add(className);
        return this;
    }

    public BaseBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public BaseBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public BaseBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
}