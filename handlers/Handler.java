package handlers;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.concurrent.ThreadPoolExecutor;

import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public abstract class Handler implements Runnable {
  protected Node node;

  public Handler(Node node) {
    this.node = node;
  }

  public abstract void run(Packet packet, PacketChannel buffer);

  @Override
  public void run() {
  }
}
