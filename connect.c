#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "connect.h"
#include "commons.h"

/**
 * @brief Tries to connect to the given socket using the given server information
 * @param [in] sock_fd The socket file descriptor to try to connect to
 * @param [in] info A pointer to one of the addresses returned by getaddrinfo
 * @return 0 on successfull connection, -1 on error
 */
int tryToConnect (int sock_fd, struct addrinfo *info) {
	#ifdef DEBUG
		printf("\n	DEBUG: tryToConnect(%d, %lx);\n", sock_fd, (unsigned long)info);
	#endif
	if (connect(sock_fd, info->ai_addr, info->ai_addrlen) == -1) {
		close(sock_fd);
		perror("Error on connectTo: ");
		return -1;
	}

	return 0;
}

/**
 * @brief Tries to connect to the server
 * @param [in] server_info Pointer returned by getaddrinfo
 * @return The sock file descriptor to use to communicate with the server, -1 on error
 */
int connectTo (struct addrinfo **server_info) {
	#ifdef DEBUG
		printf("\n	DEBUG: connectTo(%lx);\n", (unsigned long)server_info);
	#endif
	struct addrinfo *ptr;
	int sock_fd;

	for (ptr = *server_info; ptr != NULL; ptr=ptr->ai_next) {
		if ((sock_fd = socket(ptr->ai_family, ptr->ai_socktype, ptr->ai_protocol)) == -1) {
			close(sock_fd);
			perror("Error on connectTo()::socket() ");
			continue;
		}

		if (tryToConnect(sock_fd, ptr) != -1) { //connect successfuly to address
			*server_info = ptr;
			return sock_fd;
		}
	}

	return -1;
}

int setupConnection (const char *address, const char *port, struct addrinfo **server_info) {
	#ifdef DEBUG
		printf("\n	DEBUG: setupConnection('%s');\n", address);
	#endif
	struct addrinfo hints;
	int error;

	memset(&hints, 0, sizeof(hints));
	if (port != NULL) {
		hints.ai_flags 	= AI_NUMERICSERV;
	}
	hints.ai_family 	= AF_UNSPEC;
	hints.ai_socktype	= SOCK_DGRAM;

	if (port == NULL || (error = getaddrinfo(address, port, &hints, server_info)) != 0) {
		printf("Error initConnection()::getaddrinfo() '%s'\n", gai_strerror(error));
		return -1;
	}

	int ret = connectTo(server_info);
	return ret;
}
