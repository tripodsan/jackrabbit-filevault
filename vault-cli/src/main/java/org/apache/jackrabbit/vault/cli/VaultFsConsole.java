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

package org.apache.jackrabbit.vault.cli;

import org.apache.jackrabbit.vault.util.console.Console;

/**
 * {@code JcrFsConsole}...
 *
 */
public class VaultFsConsole extends Console {

    private final VaultFsApp app;

    public VaultFsConsole(VaultFsApp app) {
        super(app);
        this.app = app;
    }

    /**
     * {@inheritDoc}
     */
    protected void setup() {
        try {
            app.connect();
            app.mount(null, null, null, null, null, true);
        } catch (Exception e) {
            VaultFsApp.log.error("Error during connecting to default repository.", e);
            // ignore
        }
        super.setup();
    }
}