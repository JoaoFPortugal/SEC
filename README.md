# HDS Notary

**Course:** Highly Dependable Systems  
**University:** Instituto Superior Técnico  
**Academic year:** 2018-19

### Team

- David Gonçalves
- João Portugal
- Nuno Pinhão

### Assignment

See [Assignment.md](documentation/Assignment.md)

### Methodology

See [Methodology.md](documentation/Methodology.md)

## Testing

Tested in Ubuntu 18.04
To run:
run with java 10.0.2 and maven 3.5.2
have a copy of pteidlibj moved to /usr/local/lib
have pteidlibj renamed to pteidlibj-2.0 and install it to .m2 folder.
go to dir of module security and do mvn install
go to dir of module notary and do mvn install
do mvn compile exec:java in this dir and the notary should be up and running.
start up to 5 consoles and in each go to user and mvn compile exec:java
when asked for ID put either 1,2,3,4,5 where the password for each is "11", "22", "33", "44" or "55"
should be everything running.
There is an ongoing issue that we couldn't fix that is shared resources. Therefore when the notary stores his public key under the name serverPubKey it will do so it in his resource folders under notary and the user will try to load it and fail. To fix it, copy this serverPubKey to user resources and it should be fixed.

If when trying to do an operation there is a java.lang.unsatisfiedlinkerror try doing export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib



## To-do

- Improve README.md and documentation/Methodology.md
    - ~~How to compile app~~
    - How to test app
    - Prof: Use key stores (Java has this) instead of plain text to store the keys, because in key stores we can sign them.
- Implement communication between client and server (Producer-Consumer model)
- Prof: Using the CC to cipher everything requires entering the PIN many times, try to mitigate this. Eg. Generate new key pair that is signed with the CC). But the `transferGood` method must be necessarily ciphered with the CC.
