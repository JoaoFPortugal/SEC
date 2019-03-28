# HDS Notary

## Methodology

Our system has two separate modules, the **notary** which acts as a server and is responsible for storing the persistent data associated with the users and their goods, and the **user**, which represents an hypothetical user that wants to sell or buy goods, and can be run in multiple instances representing different users.

### Data Storage

We persist our data using **SQLite**, which is a RDMS that requires no server, therefore being thread safe and fault tolerant.

### Communication

#### Notary - User

The communication between the notary and the user is made using **TCP Sockets**, and the way that the notary deals with the multiple user connections and requests is with a **producer-consumer** pattern, that is, it has a thread that listens for incoming user connections and the requests associated, placing them in a queue, and then another thread that deals with the requests in the queue.

#### User - User

The communication between the users is done in a **peer-to-peer** fashion, that is, each user acts as a server and as a client for other users.

### Security

