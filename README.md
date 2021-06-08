# A naive example of Unix Domain Socket Server and Client in Kotlin

This example uses:
- Kotlin, Coroutines and Channels to handle multiple connections
- Unix Domain Sockets
- Java 16 channels (StandardProtocolFamily.UNIX)

Unix Domain Sockets info:
https://medium.com/swlh/getting-started-with-unix-domain-sockets-4472c0db4eb1

Requirements:

- Java 16

To start echo server via gradle just run:

```
./gradlew run
```

Check if socket server is listening:

```
ss -ax |grep server.socket
u_str LISTEN 0      50                          /home/user/server.socket 135118            * 0            
u_str ESTAB  0      0                           /home/user/server.socket 135119            * 136240   
```

You can use few socket clients to connect. When you are connected: write "quit" command to close connection.

### openbsd-netcat

```
nc -U /path/to/server.socket                                                                                                                                                                               ✘ 130 
[1]  Hello from echo server!
test message
[1]  Echo: test message
quit
[1] Bye!
```

### socat

```
socat - unix-connect:/path/to/server.socket
[1]  Hello from echo server!
test message
[1]  Echo: test message
quit
[1] Bye!
```

### example client

```
./gradlew -Pclient=true run
```



