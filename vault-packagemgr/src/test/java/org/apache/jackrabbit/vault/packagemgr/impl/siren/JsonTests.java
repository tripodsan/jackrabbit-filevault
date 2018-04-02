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
package org.apache.jackrabbit.vault.packagemgr.impl.siren;

import java.io.StringWriter;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.LinkBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;
import org.apache.sling.commons.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@code JsonTests}...
 */
public class JsonTests {

    @Test
    public void testLink() throws JSONException {
        Link link = new LinkBuilder()
                .setHref("/foo/bar")
                .setTitle("Hello")
                .addRel("self")
                .addRel("next");
        String expected =
                "{" +
                        "'rel':['next','self']," +
                        "'href':'/foo/bar'," +
                        "'title':'Hello'" +
                "}";

        expected = expected.replace('\'', '"');
        StringWriter out = new StringWriter();
        SirenJsonWriter w = new SirenJsonWriter(out);
        w.write(link);
        assertEquals(expected, out.toString());
    }

    @Test
    @Ignore
    public void testLinkEmpty() throws JSONException {
        Link link = new LinkBuilder();
        StringWriter out = new StringWriter();
        SirenJsonWriter w = new SirenJsonWriter(out);
        w.write(link);
        assertEquals("{}", out.toString());
    }

}