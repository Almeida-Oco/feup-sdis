#ifndef CONNECT_H
#define CONNECT_H

/**
 * @brief Sets up a connection with the given address using specified port
 * @param [in] address Address to connect to. Accepts ipv4 / ipv6 addresses or name of server
 * @param [in] port Port to use to connect, if NULL will use 'ftp' default
 * @return Opened server file descriptor ready for communication, -1 on error
 */
int setupConnection (const char *address, const char *port, struct addrinfo **server_info);

#endif
