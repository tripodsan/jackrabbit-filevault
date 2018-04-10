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

package org.apache.jackrabbit.vault.packagemgr.impl.rest;

import java.util.Collection;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.ActionExample;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.AnnotationTransformer;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ActionsTest {

    private static final String BASE_HREF = "http://filevault.apache.org/base/api";

    private void testAction(Action expected) {
        Collection<Action> actions = new AnnotationTransformer()
                .withModel(new ActionExample())
                .withBaseHref(BASE_HREF)
                .collectActions();
        String name = expected.getName();
        for (Action a: actions) {
            if (name.equals(a.getName())) {
                assertTrue("action: " + name, expected.equals(a));
                return;
            }
        }
        fail("action " + name + " not found.");
    }

    @Test
    public void testDefaultAction() throws Exception {
        testAction(new ActionBuilder()
                .withName("default-action")
                .withType(Action.TYPE_X_WWW_FORM_URLENCODED)
                .withPOST()
                .withTitle("")
                .withHref(BASE_HREF));
    }

    @Test
    public void testWithNameAsValue() throws Exception {
        testAction(new ActionBuilder()
                .withName("create-stuff")
                .withType(Action.TYPE_X_WWW_FORM_URLENCODED)
                .withPOST()
                .withTitle("")
                .withHref(BASE_HREF));
    }

    @Test
    public void testPutActionExample() throws Exception {
        testAction(new ActionBuilder()
                .withName("put-action")
                .withType(Action.TYPE_JSON)
                .withPUT()
                .withTitle("create new stuff")
                .withHref(BASE_HREF + "/stuff/{id}")
                .addField(new FieldBuilder()
                        .withName("title")
                        .withTitle("")
                        .withType(Field.Type.TEXT)
                        .withValue("")
                )
                .addField(new FieldBuilder()
                        .withName("flag")
                        .withTitle("")
                        .withType(Field.Type.CHECKBOX)
                        .withValue("false")
                )
        );
    }

    @Test
    public void testUploadExample() throws Exception {
        testAction(new ActionBuilder()
                .withName("upload-thumbnail")
                .withType(Action.TYPE_MULTIPART_FORM_DATA)
                .withPOST()
                .withTitle("Upload thumbnail")
                .withHref(BASE_HREF + "/thumbnail")
                .addField(new FieldBuilder()
                        .withName("title")
                        .withTitle("")
                        .withType(Field.Type.TEXT)
                        .withValue("")
                )
                .addField(new FieldBuilder()
                        .withName("file")
                        .withTitle("File to upload")
                        .withType(Field.Type.FILE)
                        .withValue("")
                )
        );
    }

    @Test
    public void testMethodToActionName() {
        assertEquals("do-post", ReflectionUtils.methodToActionName("doPost"));
        assertEquals("some-default-action", ReflectionUtils.methodToActionName("someDefaultAction"));
        assertEquals("get-it", ReflectionUtils.methodToActionName("GetIt"));
    }
}
