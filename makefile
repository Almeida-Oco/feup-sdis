#Java compiler , Java Virtual Machine , all .java files,
#classes contains all .java files but with .class instead
JAVAC=javac
JVM=java
sources = $(wildcard */*/*.java */*.java *.java)
classes = $(sources:.java=.class)

#default entry point
default: all

#builds all files
all: $(classes)

#removes .class files
clean :
	@rm -f $(classes)

backup: 
	@cp /home/jalmeida/Pictures/Moonrise.jpg /home/jalmeida/Pictures/Nature/
	@java controller.Client 1 BACKUP /home/jalmeida/Pictures/Nature/Moonrise.jpg 1

restore:
	@rm -f /home/jalmeida/Pictures/Nature/Moonrise.jpg
	@java controller.Client 1 RESTORE /home/jalmeida/Pictures/Nature/Moonrise.jpg

#what happens when trying to make a .class file
#@ hides the commands from console (they are not printed)
%.class : %.java
	@$(JAVAC) $<
	@rm -rf stored_files
