package cli;

public class User_IO {
  private static final String PLAIN = "\033[0;0m";
  private static final String BOLD  = "\033[0;1m";

  public static boolean printUsage() {
    final String prot_name = "  version          ",
                 prot_desc = "Protocol version to use ( [0-9].[0-9] )\n",
                 id_name   = "  server id        ",
                 id_desc   = "ID to assign to the server ( [0-9]+ )\n",
                 ap_name   = "  access point     ",
                 ap_desc   = "Service access point to use\n",
                 mc_name   = "  MC               ",
                 mc_desc   = "Name of the multicast control channel to use ( <ip>?:?<port> )\n",
                 mdr_name  = "  MDR              ",
                 mdr_desc  = "Name of the multicast data recovery channel to use ( <ip>?:?<port> )\n",
                 mdb_name  = "  MDB              ",
                 mdb_desc  = "Name of the multicast data channel to use ( <ip>?:?<port> )\n",
                 ip_name   = "  ip        ",
                 ip_desc   = "Standard IPv4 address (If missing 127.0.0.1 will be used)\n",
                 port_name = "  port      ",
                 port_desc = "Port number to use for the service";


    System.err.print("Usage:\n  java Server <version> <server id> <access point> <MC> <MDR> <MDB>\n\n");
    System.err.print("Arguments:\n" +
                     BOLD + prot_name + PLAIN + prot_desc +
                     BOLD + id_name + PLAIN + id_desc +
                     BOLD + ap_name + PLAIN + ap_desc +
                     BOLD + mc_name + PLAIN + mc_desc +
                     BOLD + mdr_name + PLAIN + mdr_desc +
                     BOLD + mdb_name + PLAIN + mdb_desc + "\n\n");
    System.err.print("Options:\n" +
                     BOLD + ip_name + PLAIN + ip_desc +
                     BOLD + port_name + PLAIN + port_desc + "\n\n");
    return false;
  }
}
