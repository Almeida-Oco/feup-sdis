package network.comms.sockets;

import java.util.Iterator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

public class SSLServerSocketChannel {
  ServerSocketChannel socket;
  String ip;
  int port;
  ByteBuffer my_app_data, my_net_data, peer_app_data, peer_net_data;

  private SSLServerSocketChannel(ServerSocketChannel socket, String ip, int port) {
    this.socket = socket;
    this.ip     = ip;
    this.port   = port;
  }

  public static SSLServerSocketChannel newChannel(int port) {
    try {
      ServerSocketChannel socket     = SSLChannel.newServerChannel();
      InetSocketAddress   local_addr = new InetSocketAddress(port);
      socket.configureBlocking(false);
      socket.bind(local_addr);

      return new SSLServerSocketChannel(socket, local_addr.getHostString(), port);
    }
    catch (Exception err) {
      err.printStackTrace();
    }

    return null;
  }

  public ServerSocketChannel getSocket() {
    return this.socket;
  }
}
