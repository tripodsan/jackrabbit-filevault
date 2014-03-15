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
Mapping from repository content to platform files and directories.

### Nodes of type `nt:file`

File nodes are serialized in most cases into plain platform files:

| property                         | mapping                        |
|----------------------------------|--------------------------------|
| `jcr:created`                    | n/a                            |
| `jcr:createdBy`                  | n/a                            |
| `jcr:content/jcr:primaryType`    | not preserved. defaults to `nt:resource` |
| `jcr:content/jcr:data`           | file content                   |
| `jcr:content/jcr:lastModified`   | file timestamp                 |
| `jcr:content/jcr:lastModifiedBy` | not serialized                 |
| `jcr:content/jcr:mimeType`       | mapped via extension           |
| `jcr:content/jcr:encoding`       | defaults to `utf-8` if present |
| `jcr:content/jcr:uuid`           | not serialized                 |
    
In case the node has extra content or property values that cannot be reverse mapped,
the file is treated as ordinary node (note that this is different to the original file vault behavior).

see section **all other nodes** below.

Example of non-trivial `nt:file` nodes:

* mime-type and extension don't match.
* encoding is not utf-8
* node has mixing types
* primary type is not `nt:resource` or `nt:unstructured`
* primary type is `nt:unstructured`  but node has additional child nodes or properties that are not declared by `nt:resource`

### Nodes of type `nt:folder`

Folder nodes are mapped to directories with not additional node information (i.e. no `_content.json`).

### Complete Subtrees

Subtrees that are entirely serialized based on configuration and content are represented by 1 JSON file of arbitrary depth. 

The default of node types for so called _full coverage_ aggregates is:

* rep:AccessControl
* rep:Policy
* vlt:FullCoverage
* mix:language
* sling:OsgiConfig
* cq:Widget
* cq:EditConfig
* cq:WorkflowModel

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
Todo: explain `:childOrder`

### File- and Directory names
Todo: explain escaping

### JSON Serialization
Todo: explain json serialization, especially the mapping of some of the JCR property types (date, name) and how the property type disambiguation algorithm works.

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


work in progress...
===================

14-mar-2014
---
Added experimental export type that serializes content in a JSON structure.

Example:

````
$vltng export -v -t experimental http://localhost:4502/crx /libs/wcm/foundation .
````


