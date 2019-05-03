# HDS Notary

## Methodology

Our system has three separate modules, the **notary** which acts as a server and is responsible for storing the persistent data associated with the users and their goods, the **user**, which represents an hypothetical user that wants to sell or buy goods, and can be run in multiple instances representing different users, and **security**, which is a library used by both the notary and the user, and contains security related code that is the emphasis of this project.

### Data Storage

We persist our data using **SQLite**, which is a RDMS that requires no server, is thread safe and fault tolerant, therefore assuring **durability**.

### Communication

#### Notary - User

The communication between the notary and the user is made using **TCP Sockets**. The way that the notary deals with the multiple user connections and requests is with a **producer-consumer** pattern, that is, it has one thread (the producer) that listens for incoming user connections and the requests associated, placing them in a queue, and then another number of threads (the consumers) that deal with the requests in the queue. In order to process multiple requests at the same time without compromising the individual process delay, we have as many consumer threads as CPU cores. We use a [BlockingQueue](http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html) from Java that is perfect for our needs as we do not require semaphores nor mutexes.

The user uses a class, `NotaryConnection.java`, that works as a "library" to interact with the server.

#### User - User

The communication between the users is done in a **peer-to-peer** fashion, that is, each user acts as a server and as a client for other users.

### Security

Attacks to prevent:

- Replay attacks
- Man-in-the-middle attack
- Sybil attacks
- Trading items owned by other user(s)

Each user has one pair of cryptographic keys, a public key known by everyone and a private key only known by him. The key pairs were generated using Elliptic Curve algorithm, for efficiency reasons since ECC can use keys with 224 bit size, whilst RSA requires 2048 bits for equivalent cryptographic strength, as explained [here](<https://www.globalsign.com/en/blog/elliptic-curve-cryptography/>). 

In order to prevent the private keys from falling into the wrong hands, we protected them using the `PBKDF2 `algorithm with a random salt generated using `SHA1PRNG`, and can only be accessed using a password. For each session initiated, a user is asked the password to access his private key. The decrypted private key is then loaded to a private field in the `User.java` class.

**Java KeyStore??**

The Notary is equipped with a Portuguese Citizen Card (CC), which contains an RSA key-pair of its own. The CC's public key is inside a certificate, it assures us that the Notary's CC public key is that of the original Notary and not a fake Notary. However, we assume that the keys have been previously shared and the certificate validated by the users.

**Confidentiality:**

This project does not aim to assure confidentiality.

**Integrity:**

The Notary assures **integrity** by creating a hash of the message, SHA-1 in this case (mandatory), and then signs / encrypts this hash using the CC's private key, sending the message and the signed hash together. The user then validates / decrypts the hash, generates his own hash of the message and compares both hashes to see if they are the same. If the hashes are different, then the message has been compromised and it is discarded.

The user assures integrity by following the same logic, except it uses SHA-2 to hash its messages and its keys use the EC algorithm.

**Non-repudiation:**

Since the messages are signed using the notary / users private keys (more specifically the hash is signed), and only each knows his own private-key and the password to decrypt it, we know that the message has been sent by them.

**Authenticity:**

The users know that the messages sent by the Notary are authentic because they have the public key of the Notary's CC with which they can validate the signature of the messages, by decrypting the hash and comparing with the hash they generated themselves. And the Notary knows the public keys of the users and verifies authenticity in the same manner.

**Freshness:**

Noonce

### Timing attacks (DDoS, slowloris...)

We do not deal with these attacks as they require more effort to mitigate and are outside the scope of this particular project.
