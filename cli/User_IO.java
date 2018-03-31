package cli;

import files.*;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static class
 * Handles printing information to the command-line
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class User_IO {
  /** {@link String} representing plain console output */
  private static final String PLAIN = "\033[0;0m";

  /**  {@link String} representing bold console output */
  private static final String BOLD = "\033[0;1m";

  /**  {@link String} representing underlined console output */
  private static final String UNDERLINE = "\033[0;4m";

  /** {@link String} representing bold console output */
  private static final int MAX_LINE_SIZE = 70;

  /**
   * Prints the usage of the server implementation
   */
  public static void serverUsage() {
    final String prot_name = "  version          ",
        prot_desc          = "Protocol version to use ( [0-9].[0-9] )\n",
        id_name            = "  server id        ",
        id_desc            = "ID to assign to the server ( [0-9]+ )\n",
        ap_name            = "  access point     ",
        ap_desc            = "Service access point to use ( [0-9]+ ) \n",
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

  /**
   * Prints the usage of the client implementation
   */
  public static void clientUsage() {
    final String ap_name = "  peer_ap        ",
        ap_desc          = "Client access point to use ( //<ip>:<port?>/<name> )\n",
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

  /**
   * Prints the current state of the peer
   * @param backed_up     The files that were sent for backup into the network
   * @param stored_chunks The chunks from the network that were stored locally
   * @param max_space     The maximum disk space the protocol is allowed to use
   * @param used_space    The currently disk space used by the protocol
   */
  public static void printState(ConcurrentHashMap<String, FileInfo> backed_up,
      ConcurrentHashMap<String, Vector<Chunk> > stored_chunks, int max_space, int used_space) {
    System.out.println("\n" + BOLD + "Used " + UNDERLINE + used_space +
        BOLD + " of " + UNDERLINE + max_space + BOLD + " bytes");

    System.out.println(BOLD + "\n" + center("BACKED UP FILES", MAX_LINE_SIZE) + "\n" + PLAIN);
    backed_up.forEach((file_name, info)->{
      printFileInfo(info);
    });

    System.out.println(BOLD + "\n" + center("STORED CHUNKS", MAX_LINE_SIZE) + "\n" + PLAIN);
    stored_chunks.forEach((file_id, chunks)->{
      System.out.println(BOLD + file_id + PLAIN);
      chunks.forEach((chunk)->{
        printChunkInfo("      ", "#" + chunk.getChunkN(), chunk);
      });
    });
  }

  /**
   * Prints information about a single file
   * @param info {@link FileInfo} Holds the information of the file
   */
  public static void printFileInfo(FileInfo info) {
    System.out.println(BOLD + info.getName() + PLAIN);
    System.out.println("  " + UNDERLINE + info.getID() + PLAIN + "  -  " + info.getDesiredRep());
    info.getChunks().forEach((chunk)->{
      printChunkInfo("      ", "#" + Integer.toString(chunk.getChunkN()), chunk);
    });
  }

  /**
   * Prints information about a single chunk
   * @param indentation Number of spaces to indent information with
   * @param chunk_id    The chunk name to display
   * @param chunk       {@link Chunk} Holds the information of the chunk
   */
  public static void printChunkInfo(String indentation, String chunk_id, Chunk chunk) {
    System.out.print(indentation +
        UNDERLINE + chunk_id + PLAIN + " | "
        + center(Integer.toString(chunk.getSize()), 5) + " | "
        + chunk.getActualRep() + "/"
        + chunk.getDesiredRep() + " |");

    Vector<Integer> reps = chunk.getReplicators();
    synchronized (reps) {
      for (Integer rep : reps) {
        System.out.print(" " + rep);
      }
    }
    System.out.println("");
  }

  /**
   * Centers the given text according to the given width
   * @param  text Text to be centered
   * @param  len  Width to use
   * @return      Centered string based on text and width
   */
  public static String center(String text, int len) {
    String out   = String.format("%" + len + "s%s%" + len + "s", "", text, "");
    float  mid   = (out.length() / 2);
    float  start = mid - (len / 2);
    float  end   = start + len;

    return out.substring((int)start, (int)end);
  }
}
