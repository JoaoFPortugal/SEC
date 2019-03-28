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

## Maven

### Build & Run

```sh
cd interface
mvn install

cd notary
mvn compile # Build
mvn exec:java # Run
```

### Generate Eclipse project

```sh
mvn eclipse:eclipse
```

## To-do

- ~~Create Assignment.md~~

- ~~Persist data of notary: SQLite~~

- ~~Use Maven (or Gradle) for managing the build~~

- Improve README.md
    - Will be used for Report

- Implement communication between client and server (Producer-Consumer model)
    - Implement methods of assignment

- Implement communication between users (peer-to-peer)

- Assume a fixed number of (max) users that we know beforehand, we know their keys beforehand, we know their IP and port beforehand, we know their goods beforehand. (both the notary and users know beforehand)

- Design system's architecture and communication model (assure integrity, non-repudiation, authenticity and freshness) Tip: use public-private keys for first 3 and noonces for freshness. Do not assure confidentiality!
    - Freshness: Prevent replay attacks
    - Use API used in Lab for reading citizen card
        ?- Use API for accessing card: <https://docs.oracle.com/javase/7/docs/jre/api/security/smartcardio/spec/>
    - For PKI, use João's code or openssl (save in Java format)
    - Use JavaCrypto (only 1 method used in lab is deprecated and doesn't work on Java > 8)

**How does PKI work:**

- User guarantees his authenticity by hashing the message (using for example SHA-2, hash function is passed as argument to the API) and encrypting that hash with his private key. Then the user sends the message, the encrypted hash and the hash function used (eg SHA2) to the server. The server then decrypts the hash using the user's public key, does the hash of the message and compares both hashes to see if they match.