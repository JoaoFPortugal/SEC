# HDS Notary

## Methodology

Our system has two separate modules, the **notary** which acts as a server and is responsible for storing the persistent data associated with the users and their goods, and the **user**, which represents an hypothetical user that wants to sell or buy goods, and can be run in multiple instances representing different users.

### Data Storage

We persist our data using **SQLite**, which is a RDMS that requires no server, therefore being thread safe and fault tolerant, assuring **durability**.

### Communication

#### Notary - User

The communication between the notary and the user is made using **TCP Sockets**. The way that the notary deals with the multiple user connections and requests is with a **producer-consumer** pattern, that is, it has one thread (the producer) that listens for incoming user connections and the requests associated, placing them in a queue, and then another number of threads (the consumers) that deal with the requests in the queue. In order to process multiple requests at the same time without compromising the individual process delay, we have as many consumer threads as CPU cores. We use a [BlockingQueue](http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html) from Java that is perfect for our needs as we do not require semaphores nor mutexes.

The user uses a class that works as a "library" to interact with the server.

#### User - User

The communication between the users is done in a **peer-to-peer** fashion, that is, each user acts as a server and as a client for other users.

### Security

The Notary is equipped with a Portuguese Citizen Card (CC). The Notary signs its messages by creating a hash of the messages (e.g.: SHA-2) and then encrypts this hash. The users know that the messages sent by the Notary are authentic because they have the public key of the Notary's CC with which they can validate the signature of the messages, by decrypting the hash and comparing with the hash they generated themselves.

The Notary's public key is inside a certificate. The certificate assures us that the Notary's CC public key is that of the original Notary and not a fake Notary. However, we assume that the keys have been previously shared and the certificate validated by the users.

**Integrity:**

**Non-repudiation:**

**Authenticity:**

**Freshness:**

Noonce

### Timing attacks (DDoS, slowloris...)

We do not deal with these attacks as they require more effort to mitigate and are outside the scope of this particular project.
