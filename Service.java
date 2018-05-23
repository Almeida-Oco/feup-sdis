class Service {
  public static void main(String[] args) {
    if (!Service.validArgs(args)) {
      return;
    }
    String[] ip_port = args[0].split(":");
    String   ip      = ip_port[0];
    int      port    = Integer.parseInt(ip_port[1]);
  }

  private static boolean validArgs(String[] args) {
    if (args.length != 1) {
      System.err.println("Wrong args!\n  java Service <ip>:<port>");
      return false;
    }

    if (args[0].indexOf(':') == -1) {
      System.err.println("Wrong format! <ip>:<port>\n  java Service <ip>:<port>");
    }

    return true;
  }
}
