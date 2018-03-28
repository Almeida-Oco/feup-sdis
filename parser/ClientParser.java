package parser;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parser of the Client version of the protocol
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class ClientParser {
  private static final String BACKUP  = "BACKUP";
  private static final String RESTORE = "RESTORE";
  private static final String RECLAIM = "RECLAIM";
  private static final String DELETE  = "DELETE";
  private static final String STATE   = "STATE";

  private static final String ap_regex = "\\s*//(?<ip1>\\d{1,4})\\.(?<ip2>\\d{1,4})\\.(?<ip3>\\d{1,4})\\.(?<ip4>\\d{1,4})(:(?<port>\\d{1,7}))?/(?<name>\\w+)";
  private static final Pattern pattern = Pattern.compile(ap_regex);

  /** IP of RMI registry */
  private static String ip = null;
  /** Name of the RMI object */
  private static String name = null;
  /** Port of the RMI registry */
  private static int port = -1;

  /**
   * Parses the received command line arguments
   * @param  args arguments
   * @return      Whether the parsing was successfull or not
   */
  public static boolean parseArgs(String[] args) {
    if (args.length < 2) {
      return false;
    }
    String protocol = args[1];
    int    size     = args.length;

    if (!parseAP(args[0])) {
      return false;
    }

    return protocol.equals(BACKUP) && size == 4 ||
           protocol.equals(RESTORE) && size == 3 ||
           protocol.equals(RECLAIM) && size == 3 ||
           protocol.equals(DELETE) && size == 3 ||
           protocol.equals(STATE) && size == 2;
  }

  /**
   * Parses the Access Point
   * @param  ap {@link String} representation of the Acess Point
   * @return    Whether the parsing was successfull or not
   */
  private static boolean parseAP(String ap) {
    Matcher matcher = pattern.matcher(ap);

    if (!matcher.matches()) {
      System.err.println("Access point contains error!");
      return false;
    }

    String parsed_port;
    ip = "";
    try {
      ip += matcher.group("ip1") + ".";
      ip += matcher.group("ip2") + ".";
      ip += matcher.group("ip3") + ".";
      ip += matcher.group("ip4");
      if ((parsed_port = matcher.group("port")) == null) {
        port = 1099;
      }
      else {
        port = Integer.parseInt(parsed_port);
      }
      name = matcher.group("name");
    }
    catch (IllegalStateException err) {
      System.err.println("ClientParser::parseAP -> This should not happen!");
      return false;   //This is needed for some reason
    }
    return true;
  }

  /**
   * Gets the IP of RMI registry
   * @return {@link ClientParser#ip}
   */
  public static String getIP() {
    return ip;
  }

  /**
   * Gets the name of the RMI object
   * @return {@link ClientParser#name}
   */
  public static String getName() {
    return name;
  }

  /**
   * Gets the port of RMI registry
   * @return {@link ClientParser#port}
   */
  public static int getPort() {
    return port;
  }
}
