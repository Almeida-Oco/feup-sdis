package parser;

import network.Net_IO;
import controller.ApplicationInfo;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ArgParser {
  private static final String regex    = " *((?<ip1>\\d{1,4}).(?<ip2>\\d{1,4}).(?<ip3>\\d{1,4}).(?<ip4>\\d{1,4}))?:?(?<port>\\d{1,7}) *";
  private static final Pattern pattern = Pattern.compile(regex);

  public static boolean parseArgs(String[] args) {
    byte   version;
    int    serv_id;
    String ap;
    Net_IO mc = null, mdb = null, mdr = null;

    if ((version = extractVersion(args[0])) == -1 ||
        (serv_id = extractServID(args[1])) == -1 ||
        (ap = extractAP(args[2])) == null) {
      return false;
    }

    if ((mc = extractChannel(args[3])) == null) {
      System.err.println("Failed to initialize Multicast Control Channel!");
      return false;
    }
    if ((mdb = extractChannel(args[4])) == null) {
      System.err.println("Failed to initialize Multicast Data Channel");
      return false;
    }
    if ((mdr = extractChannel(args[5])) == null) {
      System.err.println("Failed to initialize Multicast Data Recovery Channel");
      return false;
    }

    ApplicationInfo.setServId(serv_id);
    ApplicationInfo.setAP(ap);
    ApplicationInfo.setVersion(version);
    ApplicationInfo.setChannels(mc, mdb, mdr);
    return true;
  }

  private static byte extractVersion(String version) {
    byte major, minor;

    if (version.length() != 3) {
      System.err.println("Version arguments does not have exactly 3 characters!");

      return -1;
    }

    try {
      major = Byte.valueOf(String.valueOf(version.charAt(0)));
      minor = Byte.valueOf(String.valueOf(version.charAt(2)));
    }
    catch (NumberFormatException err) {
      System.err.println("Version number NaN!\n - " + err.getMessage());
      return -1;
    }
    catch (IndexOutOfBoundsException err) {
      System.err.println("This should not happen!");
      System.exit(0);
      return -1; // This is needed for some reason
    }

    return (byte)(major * 10 + minor);
  }

  private static int extractServID(String serv_id) {
    int id;

    try {
      id = Integer.parseInt(serv_id);
      return id;
    }
    catch (NumberFormatException err) {
      System.err.println("Server ID argument NaN!\n - " + err.getMessage());
      return -1;
    }
  }

  private static String extractAP(String ap) {
    if (ap.compareToIgnoreCase("UDP") != 0 &&
        ap.compareToIgnoreCase("TCP") != 0 &&
        ap.compareToIgnoreCase("RMI") != 0) {
      System.err.println("Access point argument not correct!\n - Got '" + ap + "', expected either UDP, TCP or RMI");
      return null;
    }

    return ap;
  }

  private static Net_IO extractChannel(String mc_info) {
    Matcher matcher = pattern.matcher(mc_info);
    String  ip;
    int     port;
    Net_IO  channel;

    if (!matcher.matches()) {
      System.err.println("Specified ip:port does not match expected pattern!");
      return null;
    }

    if ((ip = extractIP(matcher)) == null || (port = extractPort(matcher)) == -1) {
      return null;
    }

    channel = new Net_IO(ip, port);
    if (!channel.isReady()) {
      return null;
    }

    return channel;
  }

  private static String extractIP(Matcher matcher) {
    String ip = "";

    try {
      if (matcher.group("ip1") == null ||
          matcher.group("ip2") == null ||
          matcher.group("ip3") == null ||
          matcher.group("ip4") == null) {
        throw new IllegalArgumentException();
      }
      ip += matcher.group("ip1") + ".";
      ip += matcher.group("ip2") + ".";
      ip += matcher.group("ip3") + ".";
      ip += matcher.group("ip4");
      return ip;
    }
    catch (IllegalArgumentException err) {
      System.out.println("No ip specified! Using localhost");
      return "127.0.0.1";
    }
    catch (IllegalStateException err) {
      System.err.println("extractIP() -> This should not happen!");
      System.exit(1);
      return null;   //This is needed for some reason
    }
  }

  private static int extractPort(Matcher matcher) {
    int port = -1;

    try {
      port = Integer.parseInt(matcher.group("port"));
      if (port > 0 && port <= 65535) {
        return port;
      }
      else {
        System.err.println("Invalid port number: " + port + "\n - Port should be between [0, 65535]");
        return -1;
      }
    }
    catch (NumberFormatException err) {
      System.err.println("Port argument is not a valid port!\n - " + err.getMessage());
      return -1;
    }
    catch (IllegalArgumentException err) {
      System.err.println("No port specified for protocol!");
      return -1;
    }
  }
}
