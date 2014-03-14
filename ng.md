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

work in progress...
===================

14-mar-2014
---
Added experimental export type that serializes content in a JSON structure.

Example:

````
$vltng export -v -t experimental http://localhost:4502/crx /libs/wcm/foundation .
````


