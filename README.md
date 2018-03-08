# feup-sdis

# Packages
Currently the project has 3 major packages, network, files and controller.

## Network
Implements the protocol specified by the teacher.

Responsible for sending and receiving messages from the network.

## Files
Handles all operations related to the local file system.

## Controller
The link between Network and Files, most of the functional code is here.

### Questions for teacher:
 - After the peer received a GETCHUNK message, should the main MDB listener stop listening so that the thread generated to handle the GETCHUNK message is the only one listening to the MDB channel? If it is not supposed to stop, how do we guarantee that the main MDB listener does not capture a CHUNK message before the generated thread can do so?
