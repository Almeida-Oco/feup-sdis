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

  public static SSLServerSocketChannel newChannel(String addr, int port) {
    try {
      ServerSocketChannel socket = ServerSocketChannel.open();

      socket.configureBlocking(false);
      socket.socket().bind(new InetSocketAddress(addr, port));

      return new SSLServerSocketChannel(socket, addr, port);
    }
    catch (Exception err) {
      err.printStackTrace();
    }

    return null;
  }

  public SSLSocketChannel accept() {
    try {
      SocketChannel channel;
      while ((channel = this.socket.accept()) == null) {
      }

      channel.configureBlocking(false);

      return SSLSocketChannel.newChannel(channel, this.ip, this.port);
    }
    catch (Exception err) {
      err.printStackTrace();
    }

    return null;
  }
}
