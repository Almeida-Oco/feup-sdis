package handlers.queries;


import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

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
      System.out.println("Executing code '" + file_hash + "'");
    }
  }
}
