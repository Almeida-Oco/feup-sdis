#  FEUP - SDIS 

## Compiling the project
The project comes with a makefile.
Simply run 'make' and the program should compile.
(It may take a few seconds)

## Creating or Joining a server
Run the command:
    
    java Service <remote_ip>:<remote_port> <local_port>
    
    Example:
        java Service 120.0.1:4440 4442

If the server finds a network that the remote_ip and remote_port belong it will join that one, if not a network will be created.

## Running the Client
The main function to request code to be compiled and ran is:

    java Shell <remote_ip>:<remote_port> <local_port> <PROTOCOL>
    
    Example:
        java Shell 127.0.0.1:4440 4445 CODEONE example/Fibonacci.java

A folder called gen_code will be created with the stdout and stderr of the program that was ran.
        
## Note

We provide a the folder examples with two common programming algorithms to test the program.

For further information about how to run the protocol run either of the main entries without arguments (or with wrong arguments), that a usage information of the program will be printed.
