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
const newman = require('newman'); // require newman in your project
const path = require('path');

const port = process.argv[2] || '8080';

const collection = require('./filevault-packagemgr-tests.postman_collection.json');


const TEST_PKG_1 = path.join(__dirname, 'testfiles', 'fullpackage-1.0.zip');

// need to set the package file source correctly.
// maybe use the collection sdk....
function setPackageFile(col) {
    if (col.item) {
        col.item.forEach(setPackageFile);
    }
    else if (col.request.method === 'POST' && col.request.body && col.request.body) {
        const body = col.request.body;
        if (body.formdata) {
            body.formdata.forEach(field => {
                if (field.key === 'package' && field.type === 'file') {
                    field.src = TEST_PKG_1;
                }
            });
        } else if (body.mode === 'file') {
            body.file = TEST_PKG_1;
        }
    }
}
setPackageFile(collection);

newman.run({
    collection: collection,
    environment: {
        'name': 'test-env',
        'values': [
            {
                'key': 'HOST',
                'value': 'http://localhost:' + port,
                'type': 'text',
                'enabled': true
            }
        ],
        '_postman_variable_scope': 'environment'
    },
    reporters: 'cli'
}, function (err, summary) {
    if (err) {
        throw err;
    }
    if (summary.run.failures.length) {
        console.log('Failed with %d failing tests.', summary.run.failures.length);
        process.exit(-1);
    }
});
