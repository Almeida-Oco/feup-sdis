#Java compiler , Java Virtual Machine , all .java files,
#classes contains all .java files but with .class instead
JAVAC=javac
JVM=java
sources = $(wildcard */*/*/*.java */*/*.java */*.java *.java)
classes = $(sources:.java=.class)

#default entry point
default: all

#builds all files
all: $(classes)

#removes .class files
clean :
	@rm -f $(classes)


#what happens when trying to make a .class file
#@ hides the commands from console (they are not printed)
%.class : %.java
	@$(JAVAC) $<
