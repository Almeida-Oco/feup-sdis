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
    this.max_hash = node.getHash();
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    long          file_hash           = packet.getHash();
    String        file_content        = packet.getCode();
    TableEntry    responsible         = this.node.getResponsiblePeer(file_hash);
    PacketChannel responsible_channel = responsible.getChannel();

    if (file_hash >= this.max_hash || responsible_channel != null) {
      Handler handler = new PeerHandler(this.node, file_hash, file_content);
      PacketDispatcher.registerHandler(Packet.PEER, file_hash, handler);
      responsible_channel.sendPacket(Packet.newGetPeerPacket(Long.toString(file_hash)));
    }
    else { // I shall be executing the code
      System.out.println("Running code executer handler");

      System.out.println("Got code : " + file_content);

      try{
        ProgramRes code_results = Worker.ProgramResfromString(file_content, new String[0]);
        System.out.println("Results from code: " + code_results.toString());
      } catch (Exception e) {
        System.err.println("Unable to execute code");
      }

      //TODO send reply to reply_channel with RESULT packet (Packet.newResultPacket())
    }
  }
}
