<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  -->
  
Vault Package Manager
======================
The vault package manager provides a very simple RESTful package management that an be controlled via a json/http interface.

Testing
-------

An experimental [postman](https://www.getpostman.com/) collection resides in `src/test/postman` which can be execute via `newman`:

1. Install and run [sling start](https://sling.apache.org/documentation/getting-started.html#sling-download)
2. get newman: `npm install`
3. run tests: `npm test`

### Using a different port

By default the test run against localhost:8080. the port can be specified by adding it as argument to the test script, eg:

```bash
$ npm test 4502
```

### Testing with Maven

The Integration test can be run automatically with the `it` profile. The tests launch a sling, execute the tests and
stop it again.

```bash
$ mvn clean install -Pit
```

### Testing with Postman

The [postman collection](src/main/test/postman/filevault-packagemgr-tests.postman_collection.json) can be imported with
postman. be sure you define the `HOST` environment variable with `http://localhost:8080` or similar. Alternatively the
[localhost-8080](src/main/test/postman/localhost-8080.postman_environment.json) can be imported.


Usage
-----
The REST API is described briefly and is inspired by the [vnd.siren+json](https://github.com/kevinswiber/siren) notation.

### filevault
The `filevault` class is the entry bookmark to the filevault REST API, currently only used for the package manager. It might support the RCP service later.

````
{
  "class": [ "filevault" ],
  "properties": { 
      "version": "3.1.0", 
      "api-version": 1.0,
  },
  "links": [
    { "rel": [ "self" ], 
      "href": "/system/jackrabbit/filevault" },
    { "rel": [ "http://jackrabbit.apache.org/filevault/rels/packages" ], 
      "href": "/system/jackrabbit/filevault/packages"  }
  ]
}

````

### packages

````
{
  "class": [ "packages" ],
  "properties": { 
      "itemCount": 2,
  },
  "entities": [
    { 
      "class": [ "package-brief" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/package-brief" ], 
      "properties": {
        ...
      },
      "links": [
        { "rel": [ "self" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0?format=brief" }
        { "rel": [ "http://jackabbit.apache.org/filevault/rels/package" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0" }
        { "rel": [ "http://jackabbit.apache.org/filevault/rels/package-download" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/package1-1.0.zip" }
      ]
    },
    { 
      "class": [ "package-brief" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/package-brief" ], 
      "properties": {
        ...
      },
      "links": [
        { "rel": [ "self" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package2/1.0?format=brief" }
        { "rel": [ "http://jackabbit.apache.org/filevault/rels/package" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package2/1.0" }
        { "rel": [ "http://jackabbit.apache.org/filevault/rels/package-download" ], 
          "href": "/system/jackrabbit/filevault/packages/group1/package2/1.0/package2-1.0.zip" }
      ]
    }
  ],
  "actions": [
    {
      "name": "add-package",
      "title": "Add Package",
      "method": "POST",
      "href": "/system/jackrabbit/filevault/packages",
      "type": "multipart/form-data",
      "fields": [
        { "name": "package", "type": "file" },
        { "name": "install", "type": "boolean" }
        { "name": "overwrite", "type": "boolean" }
      ]
    }
  ],
  "links": [
    { "rel": [ "self" ], "href": "/system/jackrabbit/filevault/packages" }
  ]
}

````

#### add-package action
````
4xx Failed
Content-Type: application/vnd.siren+json

{
  "class": "error-response"
  "properties": {
    "success": "true",
    "error": "Package already exists"
  },
  "entities": [
    { 
      "class": [ "log" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/log" ],
      "href": "/system/jackrabbit/filevault/logs/1231223424234.log"
    }
  ],
  "links": [
      { "rel": ["self", "index"],
        "href": "/system/jackrabbit/filevault/packages" ??? }
  ]
}
````


````
201 Created
Content-Type: application/vnd.siren+json
Location: /system/jackrabbit/filevault/packages/group1/package2/1.0

{
  "class": "success-response"
  "properties": {
    "success": "true"
  },
  "entities": [
    { 
      "class": [ "package" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/package" ], 
      "href": "/system/jackrabbit/filevault/packages/group1/package2/1.0"
    }
  ],
  "links": [
      { "rel": ["self", "index"],
        "href": "/system/jackrabbit/filevault/packages" ??? }
  ]
}
````

### package-brief
The _package-brief_ is used in lists to provide the important properties. See the properties table of _package_ below marked specially.

### package
````
{
  "class": [ "package" ],
  "properties": { 
      ...
  },
  "entities": [
    { 
      "class": [ "thumbnail", "image"" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/thumbnail" ], 
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/thumbnail.png"
    },
    { 
      "class": [ "screenshot", "image"" ], 
      "rel": [ "http://jackabbit.apache.org/filevault/rels/screenshot" ], 
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/screenshots/1.png"
    }
  ],
  "actions": [
    {
      "name": "install-package",
      "title": "Install Package",
      "method": "POST",
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/install",
      "type": "application/x-www-form-urlencoded",
      "fields": [
      ]
    },
    {
      "name": "uninstall-package",
      "title": "Uninstall Package",
      "method": "POST",
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/uninstall",
      "type": "application/x-www-form-urlencoded",
      "fields": [
      ]
    },
    {
      "name": "delete-package",
      "title": "Delete Package",
      "method": "DELETE",
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0",
    }
  ],
  "links": [
    { "rel": [ "up", ""http://jackabbit.apache.org/filevault/rels/packages" ], 
      "href": "/system/jackrabbit/filevault/packages" }
    { "rel": [ "http://jackabbit.apache.org/filevault/rels/package-brief" ], 
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0?format=brief" }
    { "rel": [ "http://jackabbit.apache.org/filevault/rels/package-download" ], 
      "href": "/system/jackrabbit/filevault/packages/group1/package1/1.0/package1-1.0.zip" }
  ]
}

````
The _package_ provides the following properties:

(properties marked with **B** are included in the _package-brief_)


| B | Property       | Description | Example                 |
|---|----------------|-------------|-------------------------|
| X | pid      | Package Id  | foo/group1:package1:1.0 |
| X | name     | Package name | package1
| X | group    | Group name  | foo/group1
| X | version  | Version | 1.0 |
|   | build count | Value of build counter | 13 |
| X | downloadName | Filename | package1-1.0.zip |
| X | downloadSize | Size in bytes | 8124 |
|   | description | Package description | This package contains a hot fix.... |                
|   | thumbnail | href of thumbnail | property or entity ? |
| x | isInstalled | Simple flag indicating if package is installed | false |
|   | lastModified | Last modified time (ms) | 123456 |
|   | lastModifiedBy | Last modified user id | tripod |
|   | created | Created time (ms) | 123456 |
|   | createdBy | Creator user id | tripod |
|   | lastUnpacked | Time when last unpackaged (ms) | 123456 |
|   | lastUnpackedBy | User id that last unpackaged | joe |
|   | lastWrapped | Time when last wrapped (ms) | 123456 |
|   | lastWrappedBy | User id that last wrapped | joe |
|   | lastUnwrapped | Time when last unwrapped (ms) | 123456 |
|   | lastUnwrappedBy | User id that last unwrapped | joe |
|   | hasSnapshot | True if package has a snapshot | true |
|   | needsRewrapped | True if package was modified since last wrap | false |
|   | requiresRoot | True if package requires root to install it | false |
|   | requiresRestart | True if package requires restart after it was installed | false |
|   | acHandling | Access control handling mode | merge |
|   | extensions | Object that can contain extended properties | { "builtWith": "Jackrabbit" } |
|   | dependencies | Array of package dependencies | [ "foo/group1:package2" ] |
|   | providerName | Name of provider | Apache Software Foundation |
|   | providerUrl  | URL of provider | http://www.apache.org/ |
|   | providerLink | Provider specific link of this package | http://jackrabbit.apache.org/vault/example.html |
|   | workspaceFilter | Workspace filter Object (see below) | { .... } |


The _workspace filter_ is serialized as follows:

````
"workspaceFilter": {
    "filters": [
        {
            "root": "/root/path/of/rule",
            "mode": "merge",
            "default": "exclude",
            "rules": [
                {
                    "type": "include",
                    "pattern": "/root/path/of/rule/.*"
                },
                {
                ...
                }
            ]
        },
        {
        ...
        }
    ]
}
````
#### Actions

