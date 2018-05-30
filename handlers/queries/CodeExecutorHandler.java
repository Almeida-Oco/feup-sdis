package handlers.queries;


import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

import worker.*;

public class CodeExecutorHandler extends Handler {
  long max_hash;

  public CodeExecutorHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    long          file_hash   = packet.getHash();
    TableEntry    responsible = this.node.getResponsiblePeer(file_hash);
    PacketChannel channel     = responsible.getChannel();

    if (channel == null) { //I should execute it
      System.out.println("Loading content!");
      String file_content = packet.getCode();
      this.executeCode(file_content, file_hash, reply_channel);
    }
    else {
      channel.sendPacket(packet);
    }
  }

  private void executeCode(String code, long file_hash, PacketChannel reply_channel) {
    System.out.println("Running code executer handler");

    System.out.println("Got code : " + code);

    try{
      ProgramRes code_results = Worker.ProgramResfromString(code, new String[0]);

      String results = "\nProcess returned code: " + code_results.getExitval() + "\n";
      results += "Results:\n" + code_results.getStdout() + "\n\n";

      reply_channel.sendPacket(Packet.newResultPacket(Long.toString(file_hash), results));
    } catch (Exception e) {
      System.err.println("Unable to execute code\n - " + e.getMessage());
    }
  }
}
