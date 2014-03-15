vltng - Next Generation FileVault
=================================

* simple import/export
* robust
* better SCM interoperability
* json based
* no 'hidden' files


quick start
-----------

    $ mvm clean install
    $ chmod 775 vault-cli/target/appassembler/bin/vlt
    $ ln -s vault-cli/target/appassembler/bin/vlt vltng
    $ ./vltng -version
    Jackrabbit FileVault [version 3.0.1-SNAPSHOT] Copyright 2013 by Apache Software Foundation. See LICENSE.txt for more information.

Simplified Structure
--------------------
The mapping from repository content to platform files and directories (export) basically falls into 3 categories:

1. either the node structure is exported into a single binary file
2. or the node structure is exported into a single JSON file
3. or the node structure is exported into a combination of a directory and JSON file.

### Nodes of type `nt:file`

File nodes are serialized in most cases into plain platform files:

| property                         | mapping                        |
|----------------------------------|--------------------------------|
| `jcr:created`                    | n/a                            |
| `jcr:createdBy`                  | n/a                            |
| `jcr:content/jcr:primaryType`    | not always preserved. defaults to `nt:resource` |
| `jcr:content/jcr:data`           | file content                   |
| `jcr:content/jcr:lastModified`   | file timestamp                 |
| `jcr:content/jcr:lastModifiedBy` | not serialized                 |
| `jcr:content/jcr:mimeType`       | mapped via extension           |
| `jcr:content/jcr:encoding`       | defaults to `utf-8` if present |
| `jcr:content/jcr:uuid`           | not serialized                 |
    
In case the node has extra content or property values that cannot be reverse-mapped,
the file is treated as ordinary node (note that this is different to the original file vault behavior).

see section **all other nodes** below.

#### Cases of non-trivial `nt:file` nodes:

* mime-type and extension don't match. eg:

  ````
  /file.log [nt:file]
      /jcr:content [nt:resource]
          - jcr:mimeType "text/plain"
          ...
  ````

* encoding is not utf-8

  ````
  /file.txt [nt:file]
      /jcr:content [nt:resource]
          - jcr:mimeType "text/plain"
          - jcr:encoding "iso-8559-1"
          ...
  ````

* node has mixing types

  ````
  /file.txt [nt:file]
      /jcr:content [nt:resource]
          - jcr:mixinType ["mix:versionable"]
          - jcr:mimeType "text/plain"
          ...
  ````

* primary type is not `nt:resource` or `nt:unstructured`

  ````
  /file.txt [nt:file]
      /jcr:content [oak:Unstructured]
          - jcr:mimeType "text/plain"
          ...
  ````

* primary type is `nt:unstructured`  but node has additional child nodes or properties that are not declared by `nt:resource`

  ````
  /file.txt [nt:file]
      /jcr:content [nt:unstructured]
          - tags ["foo", "bar"]
          ...
  ````



### Nodes of type `nt:folder`

Folder nodes are mapped to directories with not additional node information (i.e. no `_content.json`).

### Complete Subtrees (full coverage aggregates)

Subtrees that are entirely serialized based on configuration and content are represented by 1 JSON file of arbitrary depth. 

The primary types of nodes that signal _full coverage_ aggregates are (not complete list):

* `vlt:FullCoverage`
* `rep:AccessControl`
* `rep:Policy`
* `mix:language`
* `sling:OsgiConfig`

This list should be extendable by configuration. For example Adobe CQ could add:

* `cq:Widget`
* `cq:EditConfig`
* `cq:WorkflowModel`

currently the only condition that can break a complete subtree would be if a node contains a binary property (see discussion below).

### All Other Nodes

All other nodes follow a very simple serialization where a directory with the name of the node is created and the node's properties are serialized in a JSON file named `_content.json`. The underscore keeps it on top of the directory listings and in contrast to the `.content.xml` of the old filevault, it's not treated as hidden file.

Note that the ordinary nodes always only have their properties serialized, but never their child nodes.

Example:

    /foo [sling:Folder]
        - sling:resourceType "sling/foo"
        /scripts [sling:Folder]
            /html.jsp [nt:file]
        /dialog [nt:unstuctured]
            - title "Hello"
        /install [nt:folder]
            /my-bundle.jar [nt:file]

Would be serialized in:

    foo/
        _content.json
        scripts/
            _content.json
            html.jsp
        dialog/
            _content.json
        install/
            my-bundle.jar

### Orderable Child Nodes

We want to be able to specify and maintain the order of child nodes if the node type of the parent node declares the _hasOrderableChildnodes_ flag. The ordering for full coverage serialization is derived from the ordering of the nodes in the serialized JSON. Although the JSON specification does not mandate such an ordering we still rely on it.

for all other cases, where the directory of node that has orderable childnodes, we include a special `:childOrder` array property in the `_content.json`.

Example:

the following structure:

    /foo [sling:OrderedFolder]
        - sling:resourceType "sling/foo"
        /test.jsp [nt:file]
        /dialog [nt:unstructured, vlt:FullCoverage]
        /a.txt [nt:file]
        /bar [nt:unstructured]

would be serialized:

    foo/
        _content.json
        a.txt
        bar/
          _content.json
        dialog.json
        test.jsp
        
and the `foo/_content.json` would look like:

    {
        "jcr:primaryType": "sling:OrderedFolder",
        "sling:resourceType": "sling/foo",
        ":childOrder": [
            "test.jsp", "dialog", "a.txt", "bar"
        ]
    }

### File- and Directory names

If possible the node names are directly used as file and directory names. however, not all characters that are allowed as JCR names can be used in filesystems. Illegal characters a generally escaped using the url escaping format, i.e. replacing the char by a `%` hex(char) sequence. special treatment is used for the `:` char since it's used quite often as namespace prefix separator. the `PREFIX : NAME` sequence is replaced by `_ PREFIX _ NAME`. Node names that would generate the same pattern are escaped with an extra leading `_`.

Examples:

| repository name      | platform name         |
|----------------------|-----------------------|
| `test.jpg`           | `test.jpg`            |
| `jcr:content`        | `_jcr_content`        |
| `jcr:test_image.jpg` | `_jcr_test_image.jpg` |
| `test_image.jpg`     | `test_image.jpg`      |
| `_testimage.jpg`     | `_testimage.jpg`      |
| `_test_image.jpg`    | `__test_image.jpg`    |
|                      |                       |
| `_jcr_:test.jpg`     | `__jcr_%3atest.jpg`   |
| `_jcr:test.jpg`      | `__jcr%3atest.jpg`    |
| `jcr_:test.jpg`      | `jcr_%3atest.jpg`     |

Note that the 3 last examples don't occur usually in normal content, so we can justify the extra `%` escaping.

### JSON Serialization

The serialization of JCR items to JSON is rather straight forward: JCR properties become JSON native properties, JCR multi-value properties become JSON arrays and JCR nodes become JCR objects. As mentioned above the order of the child nodes is currently preserved in the JSON, if the node is declared to have orderable child nodes. The order of non-oerdable nodes and properties is irrevant, but they are nevertheless sorted alphabetically, in order to get more stable diffs on import/export roundtrips.

Special attention need the JCR property types that don't exist or would be ambiguous in JSON. for example JCR Dates, Decimal, JCR Names, etc. Sometimes the property types are declared by the primary- or mixing type of the node, but mostly they are not. 

In order to be able to transport the property type for ambiguous situations, there are basically 3 options:

1. add an extra property that contains the property type. for example `{ "average@TypeHint": "Double" }`. but this bloats the JSON tremendously.
2. include the property type into the value. for example `{ "average": "{Double}5" }` or `{ "lastModifed": "{Date}Tue Feb 01 2011 23:40:30 GMT-0800" }`. 
   The problem here is that JCR multi value properties all have the same property type. This means when representing this in JSON, we would need to repeat the value marker for every value (ok, we could just include it in the first value :-). And we wouldn't have a nice mechanism to create an empty array of a specific type.
3. include the property type in the name. for example `{ "average{Double}": 5 }` or `{ "lastModified{Date}": "Tue Feb 01 2011 23:40:30 GMT-0800" }`. This overcomes the problems of the above solutions and we can even write `{ "timestamps{Date}": [ ] }`.

The various JCR property types are serialized as follows:

| Property Type | Type in name | Format | Example |
|---------------|--------------|--------|----------------------------------------------|
| String        | no           | as is  | `"jcr:title": "Hello, world."`               |
| Binary        | yes          | _(see below)_ | `"picture{Binary}": "toby.png"`       |     
| Boolean       | no           | as is  | `"hideInNav": false`                         |
| Long          | no           | as is  | `"counter": 1234`                            |
| Double        | no           | as is  | `"avg": -Infinity`                           |
| Decimal       | yes          | getString()  | `"big{Decimal}": "1234242325234"`      |
| Date          | yes          | ISO 8601 | `"lastModified{Date}": "Tue Feb 01 2011 23:40:30 GMT-0800"` |
| Name          | yes          | getString() | `"jcr:property{Name}": "jcr:title"` |
| Path          | yes          | getString() | `"origin{Path}": "/content/en/home"` |
| Reference     | yes          | getString() | `"ref{Reference}": "550e8400-e29b-41d4-a716-446655440000"` |
| WeakReference | yes          | getString() | `"rep:members{WeakReference}": ["550e8400-e29b-41d4-a716-446655440000"]` |
| URI           | yes          | getString() | `"redirect{Uri}": "http://localhost:4502"` |


All multi value properties except those of type `String` include their type name in the property name.

Example:

````
{
  "jcr:primaryType": "cq:Page",
  "jcr:content": {
    "cq:lastModified{Date}": "Thu Nov 11 2010 07:34:16 GMT-0800",
    "cq:lastModifiedBy": "admin",
    "cq:tags": [
      "marketing:interest/business",
      "marketing:interest/investor"
    ],
    "cq:template": "/apps/geometrixx/templates/contentpage",
    "jcr:primaryType": "cq:PageContent",
    "jcr:title": "Company",
    "sling:resourceType": "geometrixx/components/contentpage",
    "subtitle": "who we are",
    "par": {
      "jcr:primaryType": "nt:unstructured",
      "sling:resourceType": "foundation/components/parsys",
      "text_2": {
        "jcr:primaryType": "nt:unstructured",
        "sling:resourceType": "foundation/components/text",
        "text": "<p><span class=\"large\">Geometrixx was founded in 545 BC by Pythagoras of Samos.</span></p>\n",
        "textIsRich": "true"
      },
      "image": {
        "jcr:primaryType": "nt:unstructured",
        "sling:resourceType": "foundation/components/image",
        "fileReference": "/content/dam/geometrixx/offices/geo_hq.jpg",
        "imageCrop": "0,0,700,270",
        "imageRotate{Long}": 0
      }
    }
  },
  ":childNames": [
    "jcr:content",
    "discover_geometrixx",
    "management",
    "bod",
    "news",
    "press"
  ]
}  
````

### Binary properties (solution 1)
Binary properties other than the `jcr:data` in files are serialized as additional files, prefixed with `_content.` and suffixed with `.bin`. 

Example:

    /foo [nt:unstructured]
        - title "Hello"
        - picture "{some binary content of an image}"
      
Would be serialized in:

    foo/
        _content.json
        _content.picture.bin     
     
And the `_content.json` would look like:

    {
      "title": "Hello",
      "picture{Binary}": ""
    }

Note, we could also inline "small" binaries base64 encoded in the JSON but this would be very unpractical for developers to deal with.

### Binary properties (solution 2)
Binary properties other than the `jcr:data` in files are serialized as additional files in the `_content.binaries` directory and referenced in the JSON. This has the advantage that binaries in full coverage aggregate can be serialized and that the binaries can keep their original names and extensions.

Example:

    /foo [nt:unstructured]
        - title "Hello"
        - picture "{some binary content of an image}"
      
Would be serialized in:

    foo/
        _content.json
        _content.binaries/
            toby_icon.png
     
And the `_content.json` would look like:

    {
      "title": "Hello",
      "picture{Binary}": "toby_icon.png"
    }

This would also allow a more natural serialization of non-trivial `nt:file` nodes.

## Brainstorm
### What if I want to create a JSON nt:file ?

use the non-trivial nt:file serialization, e.g.:

    foo/
      my_data.json/
        _content.json
        _content._jcr_data.bin

or with the bin2 approach:

    foo/
      my_data.json/
        _content.json
        _content.binaries/
          data.json
    
or we just invent a new mime-type:

    foo/
        my_data.json-the-real-one
        
or we invent a general mimetype-extension-mapping agnostic serialization, eg:

    my_data.{application%2fjson}.json
    my_data.{text%2fplain}.log
    
  
### How do we deal with JCR namespaces

We use the default mapping of the repository we import/export against. If this is not enough, we can also specify the namespace mappings globally in a `META-INF/vault/namespaces.json` or respect the ones defined in the `META-INF/vault/nodetypes.cnd`.



work in progress...
===================

14-mar-2014
---
Added experimental export type that serializes content in a JSON structure.

Example:

````
$vltng export -v -t experimental http://localhost:4502/crx /libs/wcm/foundation .
````


