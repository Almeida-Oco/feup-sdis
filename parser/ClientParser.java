package parser;

public class ClientParser {
  private static final String BACKUP  = "BACKUP";
  private static final String RESTORE = "RESTORE";
  private static final String RECLAIM = "RECLAIM";
  private static final String DELETE  = "DELETE";
  private static final String STATE   = "STATE";

  public static boolean parseArgs(String[] args) {
    if (args.length < 2) {
      return false;
    }
    String protocol = args[1];
    int    size     = args.length;

    return protocol.equals(BACKUP) && size == 4 ||
           protocol.equals(RESTORE) && size == 3 ||
           protocol.equals(RECLAIM) && size == 3 ||
           protocol.equals(DELETE) && size == 3 ||
           protocol.equals(STATE) && size == 2;
  }
}
