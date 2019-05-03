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

Tested on Arch Linux

**Requirements:**

- JDK 8

- Gradle 5.2.1 (other versions may work)

- The `pteidlibj` library was downloaded from [here](https://www.autenticacao.gov.pt/cc-aplicacao), the Ubuntu version. It is already included in the project so no need to download. The `pteidlibj.jar` was provided to us by the course, it is meant to run with JDK < 10, which is what we're using. The `pteidlibj-2.0.jar` is meant to run with JDK 10. In order to install on linux do the following:

```sh
cd lib/linux
sudo ./install.sh
```

- Smartcard drives. Installation instructions for Arch Linux [here](<https://wiki.archlinux.org/index.php/Smartcards>).

- On Arch Linux the following is required in order to detect the card reader.

```sh
sudo systemctl start pcscd.service
pcsc_scan
```

**Running:**

1. On project root do `gradle build`. 

2. Inside `security` folder do:

    ```sh
    gradle run
    cp *_public_key.txt ../notary/src/main/resources
    mv *.txt ../user/src/main/resources
    ```

    This will create the user keys, place the public keys in the server, and place the encrypted private keys in the user(s).

3. Insert a Portuguese Citizen Card (PT-CC) in the slot before running the program.

4. On `notary` folder do: 

   ```sh
   gradle run
   ```
   
5. On `user` folder do: 

   ```sh
   gradle run
   ```
   
6. Test with user id 1, 2, 3, 4 and/or 5. Passwords are 11, 22, 33, 44 and 55 respectively.

*Note: If when trying to do an operation there is a `java.lang.UnsatisfiedLinkError` check if the `LD_LIBRARY_PATH` environment variable includes the path to the `/usr/local/lib` folder. Same for the property `java.library.path`. If you get `/usr/local/bin/pteiddialogsQTsrv: No such file or directory`, check if you ran the `install.sh` script in the `lib/linux` folder.*

## To-do

- Prof: Using the CC to cipher everything requires entering the PIN many times, try to mitigate this. Eg. Generate new key pair that is signed with the CC). But the `transferGood` method must be necessarily ciphered with the CC.