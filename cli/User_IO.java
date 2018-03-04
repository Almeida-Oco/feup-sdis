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

  public static void main(String[] args) {
    User_IO input = new User_IO(null);

    input.run();
  }

  public User_IO(SynchronousQueue<String> queue) {
    this.task_queue = queue;
    this.reader     = new Scanner(System.in);
  }

  private String readLine() {
    try {
      String line = this.reader.nextLine();
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

    System.out.print("Welcome to the p2p backup system!\nType 'help' for possible commands\nEnjoy :-)\n\n > ");

    while (response == null || !response.equalsIgnoreCase("EXIT!")) {
      if ((line = this.readLine()) != null) {
        response = this.processInput(line);
        System.out.println("  RESPONSE = " + response);

        /// response contains the string to send to the other threads

        System.out.print(" > ");
      }
    }
  }

  private String processInput(String line) {
    Scanner in = new Scanner(line);

    if (!in.hasNext()) {
      return null;
    }
    String command = in.next();
    if (command.equals("help")) {
      this.printHelp();
      return null;
    }
    else if (command.equals("exit")) {
      return "EXIT!";
    }
    else if (command.equals("status")) {
      return "STATUS";
    }
    else if (command.equals("backup")) {
      return this.processBackup(in);
    }
    else if (command.equals("recover")) {
      return this.processRecover(in);
    }
    else if (command.equals("remove")) {
      return this.processRemove(in);
    }
    else if (command.equals("set_space")) {
      return this.processSpace(in);
    }
    System.out.println("Unknown command!");
    return null;
  }

  private String processBackup(Scanner args) {
    if (args.) {
      if (!args.hasNext()) {
        System.out.println("No file name found!");
        this.printBackupHelp();
        return null;
      }
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
    if (!args.hasNext()) {
      System.out.println("No space amount found!");
      this.printSpaceHelp();
      return null;
    }
    String amount_str = args.next();
    long   amount     = this.extractSpace(amount_str);
    if (amount == -1) {
      this.printSpaceHelp();
      return null;
    }

    return "SPACE " + amount;
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

    System.out.print("\nUsage: <command> [<args>...]\n\nCommands:\n"
                     + BOLD + backup_name + PLAIN + backup_desc
                     + BOLD + recover_name + PLAIN + recover_desc
                     + BOLD + remove_name + PLAIN + remove_desc
                     + BOLD + space_name + PLAIN + space_desc
                     + BOLD + status_name + PLAIN + status_desc
                     + BOLD + help_name + PLAIN + help_desc
                     + BOLD + exit_name + PLAIN + exit_desc
                     + "\n");
  }

  private void printBackupHelp() {
    System.out.print("\nUsage: backup <file_name> <rep_degree>\n\n"
                     + BOLD + "  file_name   " + PLAIN + "Path to the file to backup\n"
                     + BOLD + "  rep_degree  " + PLAIN + "Number of nodes that will store the file\n"
                     + "\n");
  }

  private void printRecoverHelp() {
    System.out.print("\nUsage: recover <file_name>\n\n"
                     + BOLD + "  file_name   " + PLAIN + "Path to the file to recover\n"
                     + "\n");
  }

  private void printRemoveHelp() {
    System.out.print("\nUsage: remove <file_name>\n\n"
                     + BOLD + "  file_name   " + PLAIN + "Path to the file to backup\n"
                     + "\n");
  }

  private void printSpaceHelp() {
    System.out.print("\nUsage: set_space <amount>[magnitude]\n\n"
                     + BOLD + "  amount      " + PLAIN + "Amount of space to assign to the service\n"
                     + BOLD + "  magnitude   " + PLAIN + "'GB', 'MB' or 'KB' (Default = 'KB') \n"
                     + "\n");
  }

  public long extractSpace(String txt) {
    long    multiplier = 1;
    int     size       = txt.length();
    boolean has_mult   = false;
    long    number     = 0;

    for (int i = size - 1; i >= 0; i--) {
      char chr       = txt.charAt(i);
      int  num_value = Character.getNumericValue(chr);
      if (chr >= '0' && chr <= '9') {
        number += num_value * Math.pow(10, (has_mult ? size - i - 3 : size - i));
      }
      else if (chr == 'B' && number == 0) {
        has_mult = true;
        char mult = txt.charAt(--i);
        multiplier = (mult == 'G' ? 1048576 : (mult == 'M' ? 1024 : (mult == 'K' ? 1 : -1)));
        if (multiplier == -1) {
          System.out.println("Unknown magnitude " + mult + chr);
          return -1;
        }
      }
      else {
        System.out.println("Malformed space value!");
        return -1;
      }
    }
    System.out.println("Number = " + number + "\n Mult = " + multiplier);
    return number * multiplier;
  }

  private boolean isNumber(String txt) {
    for (int i = 0; i < txt.length(); i++) {
      if (!(txt.charAt(i) >= '0' && txt.charAt(i) <= '9')) {
        return false;
      }
    }
    return true;
  }

  private long stringToMult(String mult) {
    if (mult.equals("GB")) {
      return 1048576;
    }
    else if (mult.equals("MB")) {
      return 1024;
    }
    else if (mult.equals("KB")) {
      return 1;
    }
    else {
      System.out.println("Unknown magnitude '" + mult + "'");
      return -1;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    this.reader.close();
  }
}
