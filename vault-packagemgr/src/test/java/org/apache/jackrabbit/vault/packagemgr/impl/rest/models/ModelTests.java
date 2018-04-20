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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.models;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonStructure;

import org.apache.jackrabbit.vault.packagemgr.impl.models.Filevault;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.ActionExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.EntitiesExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.LinkExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.PropertyExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfoLoader;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.ResourceContext;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ModelTests {

    private static final ModelInfoLoader loader = new ModelInfoLoader();

    private static final URI BASE_HREF = URI.create("http://localhost:8080/system");

    private void testModelJson(Object model, String filename) throws IOException, URISyntaxException {
        Entity entity = new ResourceContext()
                .withModel(model)
                .withInfo(loader.load(model.getClass()))
                .withInfoLoader(loader)
                .withBaseURI(BASE_HREF)
                .buildEntity();

        StringWriter out = new StringWriter();
        SirenJsonWriter w = new SirenJsonWriter(out, true);
        w.write(entity);
        w.close();

        JsonStructure expectedObject = Json.createReader(getClass().getResourceAsStream(filename)).read();
        JsonStructure actualObject = Json.createReader(new StringReader(out.toString())).read();
        assertEquals("model: " + filename, expectedObject, actualObject);
    }

    @Test
    public void testFileVault() throws Exception {
        testModelJson(new Filevault(), "filevault.json");
    }

    @Test
    public void testPropertyExample() throws Exception {
        testModelJson(new PropertyExample(), "property_example.json");
    }

    @Test
    public void testLinkExample() throws Exception {
        testModelJson(new LinkExample(), "link_example.json");
    }

    @Test
    public void testEntitiesExample() throws Exception {
        testModelJson(new EntitiesExample(), "entities_example.json");
    }

    @Test
    public void testSubEntitiesExample() throws Exception {
        testModelJson(new EntitiesExample.SimpleEntity("1", "Hello, world"), "sub_entities_example.json");
    }

    @Test
    public void testActionExample() throws Exception {
        testModelJson(new ActionExample(), "action_example.json");
    }

    @Test
    public void testActionReferenceExample() throws Exception {
        testModelJson(new ActionExample.ActionReferenceExample(), "action_reference_example.json");
    }
}
