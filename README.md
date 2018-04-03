#  FEUP - SDIS 

## Compiling the project
The project comes with a makefile that assumes the following folder structure:

    | -- makefile
    | -- cli
         | -- User_IO.java
      -- controller
         | -- ApplicationInfo.java
         | -- ChannelListener.java
         | -- Client.java
         | -- DispatcherInterface.java
         | -- Handler.java
         | -- Pair.java
         | -- Server.java
         | -- SignalHandler.java
           -- client
              | -- BackupHandler.java
              | -- CheckHandler.java
              | -- DeleteHandler.java
              | -- Dispatcher.java
              | -- ReclaimHander.java
              | -- RestoreHandler.java
              | -- StateHandler.java
           -- server
              | -- ChkchunkHandler.java
              | -- DeleteHandler.java
              | -- GetchunkHandler.java
              | -- PutchunkHandler.java
              | -- RemovedHandler.java
    | -- files
         | -- Chunk.java
         | -- ChunkStorer.java
         | -- File_IO.java
         | -- FileHandler.java
         | -- FileInfo.java
         | -- LocalChunk.java
         | -- NetworkChunk.java
         | -- StringToHex.java
    | -- network
         | -- Net_IO.java
         | -- PacketInfo.java
    | -- parser
         | -- ClientParser.java
         | -- ServerParser.java
       
Simply run 'make' and the program should compile.
(It may take a few seconds)

## Running the Peer
The main function to initiate a new program is located in 'controller.Server', so running is as follow
    
    java controller.Server <version> <server_id> <access_point> <MC> <MDR> <MDB>
    
    Example:
        java controller.Server 1.0 1 8000 224.0.0.1:8001 224.0.0.2:8002 224.0.0.3:8003

## Running the Client
The main function to initiate client protocol is located in 'controller.Client'

    java controller.Client  <peer_ap> <sub_protocol> <operand1> <operand2>
    
    Example:
        java controller.Client 1 BACKUP example.txt 1
        
## Note
For further information about how to run the protocol run either of the main entries without arguments (or with wrong arguments), that a usage information of the program will be printed.
