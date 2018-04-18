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

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.ActionExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ActionInfo;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Field;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.ActionBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.ModelInfoBuilder;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.FieldBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ActionsTest {

    private static final String BASE_HREF = "http://filevault.apache.org/base/api";

    private void testAction(Action expected) throws URISyntaxException {
        Collection<ActionInfo> actions = new ModelInfoBuilder()
                .withModel(new ActionExample())
                .withSelfURI(new URI(BASE_HREF))
                .collectActions();
        String name = expected.getName();
        for (ActionInfo ai: actions) {
            Action a = ai.getSirenAction();
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
                .withType("")
                .withMethod(Action.Method.POST)
                .withTitle("")
                .withHref(BASE_HREF)
                .build()
        );
    }

    @Test
    public void testWithNameAsValue() throws Exception {
        testAction(new ActionBuilder()
                .withName("create-stuff")
                .withType("")
                .withMethod(Action.Method.POST)
                .withTitle("")
                .withHref(BASE_HREF)
                .build()
        );
    }

    @Test
    public void testPutActionExample() throws Exception {
        testAction(new ActionBuilder()
                .withName("put-action")
                .withType(Action.TYPE_JSON)
                .withMethod(Action.Method.PUT)
                .withTitle("create new stuff")
                .withHref(BASE_HREF + "/stuff/{id}")
                .addField(new FieldBuilder()
                        .withName("title")
                        .withTitle("")
                        .withType(Field.Type.TEXT)
                        .withValue("")
                        .build()
                )
                .addField(new FieldBuilder()
                        .withName("flag")
                        .withTitle("")
                        .withType(Field.Type.CHECKBOX)
                        .withValue("false")
                        .build()
                )
                .build()
        );
    }

    @Test
    public void testUploadExample() throws Exception {
        testAction(new ActionBuilder()
                .withName("upload-thumbnail")
                .withType(Action.TYPE_MULTIPART_FORM_DATA)
                .withMethod(Action.Method.POST)
                .withTitle("Upload thumbnail")
                .withHref(BASE_HREF + "/thumbnail")
                .addField(new FieldBuilder()
                        .withName("title")
                        .withTitle("")
                        .withType(Field.Type.TEXT)
                        .withValue("")
                        .build()
                )
                .addField(new FieldBuilder()
                        .withName("file")
                        .withTitle("File to upload")
                        .withType(Field.Type.FILE)
                        .withValue("")
                        .build()
                )
                .build()
        );
    }

    @Test
    public void testMethodToActionName() {
        assertEquals("do-post", ReflectionUtils.methodToActionName("doPost"));
        assertEquals("some-default-action", ReflectionUtils.methodToActionName("someDefaultAction"));
        assertEquals("get-it", ReflectionUtils.methodToActionName("GetIt"));
    }

    @Test
    public void testInvalidActions() throws URISyntaxException {
        ActionExample.InvalidActionExamples model = new ActionExample.InvalidActionExamples();
        ModelInfoBuilder tf = new ModelInfoBuilder()
                .withModel(model)
                .withSelfURI(new URI(BASE_HREF));
        for (Method method: model.getClass().getMethods()) {
            ApiAction annotation = method.getAnnotation(ApiAction.class);
            if (annotation != null) {
                try {
                    tf.buildAction(annotation, method);
                    fail("expected " + method + " to fail");
                } catch (Exception e) {
                    // ok
                }
            }
        }
    }
}
