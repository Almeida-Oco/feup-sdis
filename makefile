#Java compiler , Java Virtual Machine , all .java files,
#classes contains all .java files but with .class instead
JAVAC=javac
JVM=java
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

#main entry point (Name of the file containing the main main)
MAIN = Game

#default entry point
default: all

#builds all files
all: $(classes)

#removes .class files
clean :
	rm -f *.class

#what happens when trying to make a .class file
#@ hides the commands from console (they are not printed)
%.class : %.java
	@$(JAVAC) $<

#make run entry point, basically java MAIN
run: $(MAIN).class
@$(JVM) $(MAIN)
