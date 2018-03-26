#Java compiler , Java Virtual Machine , all .java files,
#classes contains all .java files but with .class instead
JAVAC=javac
JVM=java
sources = $(wildcard */*/*.java */*.java *.java)
classes = $(sources:.java=.class)

ifeq (reclaim,$(firstword $(MAKECMDGOALS)))
  # use the rest as arguments for "reclaim"
  SPACE := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  # ...and turn them into do-nothing targets
  $(eval $(SPACE):;@:)
endif

#default entry point
default: all

#builds all files
all: $(classes)

#removes .class files
clean :
	@rm -f $(classes)
	@rm -rf stored_files

backup: 
	@cp /home/jalmeida/Pictures/Example2.png /home/jalmeida/Pictures/Example.png
	@java controller.Client 1 BACKUP /home/jalmeida/Pictures/Example.png 1

restore:
	@rm /home/jalmeida/Pictures/Example.png
	@java controller.Client 1 RESTORE /home/jalmeida/Pictures/Example.png

delete:
	@cp /home/jalmeida/Pictures/Example2.png /home/jalmeida/Pictures/Example.png
	@java controller.Client 1 DELETE /home/jalmeida/Pictures/Example.png

reclaim:
	@java controller.Client 2 RECLAIM $(word 1, $(SPACE))


#what happens when trying to make a .class file
#@ hides the commands from console (they are not printed)
%.class : %.java
	@$(JAVAC) $<
	@rm -rf stored_files
