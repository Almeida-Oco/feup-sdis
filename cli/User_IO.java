package cli;

public class User_IO {
  private static final String PLAIN     = "\033[0;0m";
  private static final String BOLD      = "\033[0;1m";
  private static final String UNDERLINE = "\033[0;4m";

  public static void serverUsage() {
    final String prot_name = "  version          ",
        prot_desc          = "Protocol version to use ( [0-9].[0-9] )\n",
        id_name            = "  server id        ",
        id_desc            = "ID to assign to the server ( [0-9]+ )\n",
        ap_name            = "  access point     ",
        ap_desc            = "Service access point to use ( .* ) \n",
        mc_name            = "  MC               ",
        mc_desc            = "Name of the multicast control channel to use ( <ip>?:?<port> )\n",
        mdr_name           = "  MDR              ",
        mdr_desc           = "Name of the multicast data recovery channel to use ( <ip>?:?<port> )\n",
        mdb_name           = "  MDB              ",
        mdb_desc           = "Name of the multicast data channel to use ( <ip>?:?<port> )\n",
        ip_name            = "  ip        ",
        ip_desc            = "Standard IPv4 address (If missing 127.0.0.1 will be used)\n",
        port_name          = "  port      ",
        port_desc          = "Port number to use for the service",
        usage   = "  java controller.Server <version> <server id> <access point> <MC> <MDR> <MDB>",
        example = "  java controller.Server 1.0 1 8000 224.0.0.1:8001 224.0.0.2:8002 224.0.0.3:8003";


    System.err.print("Usage:\n" + usage + "\n" + example + "\n\n");

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
  }

  public static void clientUsage() {
    final String ap_name = "  peer_ap        ",
        ap_desc          = "Client access point to use ( .* )\n",
        prot_name        = "  sub protocol   ",
        prot_desc        = "Protocol to initiate. ( <protocol> )\n",
        op1_name         = "  operand1       ",
        op1_desc         = "First operand of the protocol, optional depending on protocol (see below)\n",
        op2_name         = "  operand2       ",
        op2_desc         = "Second operand of the protocol, optional depending on protocol (see below)\n",
        backup_name      = "  BACKUP    ",
        backup_desc      = "Backs up the file specified in " + UNDERLINE + "operand1" + PLAIN + " with the replication degree in " + UNDERLINE + "operand2\n" + PLAIN,
        restore_name     = "  RESTORE   ",
        restore_desc     = "Restores the file specified in " + UNDERLINE + "operand1" + PLAIN + " from the network\n",
        delete_name      = "  DELETE    ",
        delete_desc      = "Deletes the file specified in " + UNDERLINE + "operand1" + PLAIN + " from the network\n",
        reclaim_name     = "  RECLAIM   ",
        reclaim_desc     = "Reclaims the memory space specified in " + UNDERLINE + "operand1" + PLAIN + " (memory in KB)\n",
        state_name       = "  STATE     ",
        state_desc       = "Retrives the current status of the peer (no operand needed)\n",
        usage            = "  java controller.Client <peer_ap> <sub protocol> <operand1> <operand2>",
        example          = "  java controller.Client 1 BACKUP example.txt 1";

    System.out.print("Usage:\n" + usage + "\n" + example + "\n\n");

    System.err.print("Arguments:\n" +
        BOLD + ap_name + PLAIN + ap_desc +
        BOLD + prot_name + PLAIN + prot_desc +
        BOLD + op1_name + PLAIN + op1_desc +
        BOLD + op2_name + PLAIN + op2_desc + "\n\n");
    System.err.print("Protocols:\n" +
        BOLD + backup_name + PLAIN + backup_desc +
        BOLD + restore_name + PLAIN + restore_desc +
        BOLD + delete_name + PLAIN + delete_desc +
        BOLD + reclaim_name + PLAIN + reclaim_desc +
        BOLD + state_name + PLAIN + state_desc + "\n\n");
  }

  public static void printState(Files files) {
    files.getFiles().forEach((file_name, info)->{
      System.out.println(BOLD + file_name + PLAIN);
      info.forEach((number, size)->{
        System.out.println(BOLD + "  #" + number + PLAIN + " - (" + UNDERLINE + size + PLAIN + "KB)");
      });
    });
  }
}
