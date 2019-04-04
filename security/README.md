# HDS Notary - Security module

This module has the security classes needed to be used by both the `notary` and the `user`.

Run `mvn install` in this module before trying to run both the `user` and the `notary`.

Moreover it also generates the initial key pairs for all the users.

## Generating key pairs

```sh
mvn compile
cd target/classes
java hds_security.GenPubAndPrivKeys
cp *.txt ../../../user/src/main/resources
cp *_public_key.txt ../../../notary/src/main/resources
```

