Tested in Ubuntu 18.04
To run:
run with java 8 and gradle 5.4.1
- The `pteidlibj` library was downloaded from [here](https://www.autenticacao.gov.pt/cc-aplicacao), the Ubuntu version. It is already included in the project so no need to download. The `pteidlibj.jar` was provided to us by the course, it is meant to run with JDK < 10, which is what we're using. The `pteidlibj-2.0.jar` is meant to run with JDK 10. In order to install on linux do the following:

```sh
cd lib/linux
sudo ./install.sh
```

start up to 5 consoles and in each go to notary and type gradle run
when asked for server port put 6066, 6067, 6068, 6069 and 6070, the passwords for each are s1, s2, s3, s4, s5, finally when asked if you want to use cc choose no(0)
if you want to choose with cc then this only works with one server and that server port must be 6066 with password s1
start up to 5 consoles and in each go to user and type gradle run
when asked for ID put either 1,2,3,4,5 where the password for each is "11", "22", "33", "44" or "55"
when asked if server is running with cc, respond with what you chose before yes(1)/no(0).
everything should be running.
There is an ongoing issue that we couldn't fix that is shared resources. Therefore when the notary stores his public key under the name serverPubKey it will do so it in his resource folders under notary and the user will try to load it and fail. To fix it, copy this serverPubKey to user resources and it should be fixed.

If when trying to do an operation there is a java.lang.unsatisfiedlinkerror try doing export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
