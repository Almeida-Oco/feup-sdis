package handlers;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.concurrent.ThreadPoolExecutor;

import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketBuffer;

public abstract class Handler implements Runnable {
  Node myself;

  public Handler(Node node) {
    this.myself = node;
  }

  public abstract void run(Packet packet, PacketBuffer buffer);
}
