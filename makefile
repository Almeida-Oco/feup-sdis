CC=g++
ODIR=./obj
CFLAGS=-Wall -Wextra -lm



default: service

DEPS = connect.h commons.h
_OBJ = connect.o main.o
OBJ = $(patsubst %, $(ODIR)/%,$(_OBJ))

$(ODIR)/%.o: %.c $(DEPS)
	@mkdir -p ./obj
	@$(CC) -g -c -o $@ $< $(CFLAGS)

service: $(OBJ)
	@gcc -g -o $@ $^ $(CFLAGS) $(LIBS)

.PHONY: clean

clean:
	@rm -fr $(ODIR) service *.h.gch
