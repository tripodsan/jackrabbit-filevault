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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiAction;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiField;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;

@ApiModel
public class ActionExample {

    @ApiAction
    public void defaultAction() {

    }

    @ApiAction("create-stuff")
    public void actionWithNameAsValue() {

    }

    @ApiAction(
            method = ApiAction.Method.PUT,
            name = "put-action",
            type = ApiAction.TYPE_JSON,
            title = "create new stuff",
            href = "/stuff/{id}",
            fields = {
                @ApiField("title"),
                @ApiField(name = "flag", type = ApiField.Type.CHECKBOX, defaultValue = "false")
            }
    )
    public void doPut() {

    }

    @ApiAction(
            name = "upload-thumbnail",
            type = ApiAction.TYPE_MULTIPART_FORM_DATA,
            title = "Upload thumbnail",
            href = "/thumbnail",
            fields = {
                @ApiField("title"),
                @ApiField(name = "file", type= ApiField.Type.FILE, title = "File to upload"),
            }
    )
    public void doUpload() {

    }
}
