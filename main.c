#include "connect.h"
#include <stdio.h>

int main() {
	struct addrinfo *connection;
	int ret = setupConnection("127.0.0.1", "8000", &connection);
	printf("%d\n", ret);
	close(ret);
}
