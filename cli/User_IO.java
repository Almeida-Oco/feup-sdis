package cli;

import java.util.concurrent.SynchronousQueue;
import java.util.Scanner;
import java.util.NoSuchElementException;

/// Possible commands:
///
/// backup <file_name> <replication_degree>
/// recover <file_name>
/// remove <file_name>
/// set_space <amount_of_space>[GB/Gb or MB/Mb or KB/Kb]
/// status
/// exit
/// help


public class User_IO extends Thread {
  private static final String PLAIN = "\033[0;0m";
  private static final String BOLD  = "\033[0;1m";

  Scanner reader;
  SynchronousQueue<String> task_queue;


  public User_IO(SynchronousQueue<String> queue) {
    this.task_queue = queue;
    this.reader     = new Scanner(System.in);
  }

  private void print(String information) {
    System.out.println("\n" + information + "\n > ");
  }

  private String readLine() {
    try {
      String line = this.reader.nextLine();
      System.out.println(" > ");
      return line;
    }
    catch (NoSuchElementException err) {
      System.err.println("No line to read from!\n " + err.getMessage());
      return null;
    }
    catch (IllegalStateException err) {
      System.err.println("Failed to read line!\n " + err.getMessage());
      return null;
    }
  }

  public void run() {
    String response = "", line;

    System.out.println("Welcome to the p2p backup system!\nType 'help' for possible commands\nEnjoy\n\n > ");
    while (!response.equalsIgnoreCase("exit")) {
      if ((line = this.readLine()) != null) {
      }
    }
  }

  private boolean processInput(String line) {
    Scanner in = new Scanner(line);

    if (!in.hasNext()) {
      return false;
    }
    String command = in.next();
    if (command.equals("help")) {
      this.printHelp();
      return false;
    }
    else if (command.equals("exit")) {
      line = "EXIT!";
      return true;
    }
    else if (command.equals("status")) {
      line = "STATUS";
      return true;
    }
    else if (command.equals("backup")) {
      line = this.processBackup(in);
      return true;
    }
    else if (command.equals("recover")) {
      line = this.processRecover(in);
      return true;
    }
    else if (command.equals("remove")) {
      line = this.processRemove(in);
      return true;
    }
    else if (command.equals("set_space")) {
      line = this.processSpace(in);
      return true;
    }
    System.out.println("Unknown command!");
    this.printHelp();
    return false;
  }

  private String processBackup(Scanner args) {
    if (!args.hasNext()) {
      System.out.println("No file name found!");
      this.printBackupHelp();
      return null;
    }
    String file_name = args.next();
    if (!args.hasNextInt()) {
      System.out.println("No replication degree found!");
      this.printBackupHelp();
      return null;
    }

    int rep_degree = args.nextInt();
    if (args.hasNext()) {
      System.out.println("Too many arguments found!");
      this.printBackupHelp();
      return null;
    }
    return "BACKUP " + file_name + " " + rep_degree;
  }

  private String processRecover(Scanner args) {
    if (!args.hasNext()) {
      System.out.println("No file name found!");
      this.printRecoverHelp();
      return null;
    }
    String file_name = args.next();

    if (args.hasNext()) {
      System.out.println("Too many arguments found!");
      this.printRecoverHelp();
      return null;
    }
    return "RECOVER " + file_name;
  }

  private String processRemove(Scanner args) {
    if (!args.hasNext()) {
      System.out.println("No file name found!");
      this.printRemoveHelp();
      return null;
    }
    String file_name = args.next();

    if (args.hasNext()) {
      System.out.println("Too many arguments found!");
      this.printRemoveHelp();
      return null;
    }
    return "REMOVE " + file_name;
  }

  private String processSpace(Scanner args) {
    return null;
  }

  private void printHelp() {
    final String backup_name  = "  backup",
                 backup_desc  = "      Backs up a file\n",
                 recover_name = "  recover",
                 recover_desc = "     Recovers the file\n",
                 remove_name  = "  remove",
                 remove_desc  = "      Removes the file\n",
                 space_name   = "  set_space",
                 space_desc   = "   Sets the space to be used by the program\n",
                 status_name  = "  status",
                 status_desc  = "      Status of files\n",
                 help_name    = "  help",
                 help_desc    = "        Print this message\n",
                 exit_name    = "  exit",
                 exit_desc    = "        Stops the program exectution\n";

    System.out.println("\nUsage: <command> [<args>...]\n\nCommands:\n"
                       + BOLD + backup_name + PLAIN + backup_desc
                       + BOLD + recover_name + PLAIN + recover_desc
                       + BOLD + remove_name + PLAIN + remove_desc
                       + BOLD + space_name + PLAIN + space_desc
                       + BOLD + status_name + PLAIN + status_desc
                       + BOLD + help_name + PLAIN + help_desc
                       + BOLD + exit_name + PLAIN + exit_desc
                       + "\n > ");
  }

  private void printBackupHelp() {
    System.out.println("\nUsage: backup <file_name> <rep_degree>\n\n"
                       + BOLD + "  file_name   " + PLAIN + "Path to the file to backup\n"
                       + BOLD + "  rep_degree  " + PLAIN + "Number of nodes that will store the file\n"
                       + "\n > ");
  }

  private void printRecoverHelp() {
    System.out.println("\nUsage: recover <file_name>\n\n"
                       + BOLD + "  file_name   " + PLAIN + "Path to the file to recover\n"
                       + "\n > ");
  }

  private void printRemoveHelp() {
    System.out.println("\nUsage: remove <file_name>\n\n"
                       + BOLD + "  file_name   " + PLAIN + "Path to the file to backup\n"
                       + "\n > ");
  }

  private void printSpaceHelp() {
    System.out.println("\nUsage: set_space <amount>[magnitude]\n\n"
                       + BOLD + "  amount      " + PLAIN + "Amount of space to assign to the service\n"
                       + BOLD + "  magnitude   " + PLAIN + "'GB', 'MB' or 'KB' (Default = 'KB') \n"
                       + "\n > ");
  }

  @Override
  protected void finalize() throws Throwable {
    this.reader.close();
  }
}
