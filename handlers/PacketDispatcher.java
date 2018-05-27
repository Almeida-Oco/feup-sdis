package handlers;

import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import handlers.queries.*;
import handlers.replies.*;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketBuffer;

public class PacketDispatcher {
  /** Maps message type and hash to a Handler */
  private static ConcurrentHashMap<String, ConcurrentHashMap<Long, Handler> > type_hash_handler;

  private static ConcurrentHashMap<String, Handler> query_types;

  static {
    type_hash_handler = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Handler> >(4, 1);
    query_types       = new ConcurrentHashMap<String, Handler>(6, 1);
    type_hash_handler.put(Packet.RESULT, new ConcurrentHashMap<Long, Handler>(32));
    type_hash_handler.put(Packet.PEER, new ConcurrentHashMap<Long, Handler>(32));
    type_hash_handler.put(Packet.HEARTBEAT, new ConcurrentHashMap<Long, Handler>(32));
    type_hash_handler.put(Packet.FATHER, new ConcurrentHashMap<Long, Handler>(32));
  }

  public static void initQueryHandlers(Node myself) {
    query_types.put(Packet.CODE, (Handler)(new CodeExecutorHandler(myself)));
    query_types.put(Packet.NEW_PEER, (Handler)(new PeerJoinHandler(myself)));
    query_types.put(Packet.ALIVE, (Handler)(new KeepAliveHandler(myself)));
    query_types.put(Packet.PREDECESSOR, (Handler)(new PredecessorHandler(myself)));
    query_types.put(Packet.GET_PEER, (Handler)(new GetPeerHandler(myself)));
    query_types.put(Packet.LEAVE, (Handler)(new DropoutHandler(myself)));
  }

  public static void handlePacket(Packet packet, PacketBuffer buffer) {
    String type = packet.getType();

    if (query_types.contains(type)) {
      handleQuery(buffer, packet, type);
    }
    else {
      handleReply(buffer, packet, type);
    }
  }

  private static void handleQuery(PacketBuffer buffer, Packet packet, String type) {
    Handler handler = query_types.get(type);

    handler.run(packet, buffer);
  }

  private static void handleReply(PacketBuffer buffer, Packet packet, String type) {
    ConcurrentHashMap<Long, Handler> hash_handler = type_hash_handler.get(type);
    if (hash_handler != null) {
      Handler handler = hash_handler.get(packet.getHash());
      if (handler != null) {
        handler.run(packet, buffer);
      }
    }
  }

  public static boolean registerHandler(String type, long hash, Handler handler) {
    ConcurrentHashMap<Long, Handler> hash_handler = type_hash_handler.get(type);
    if (!hash_handler.containsKey(hash)) {
      hash_handler.put(hash, handler);
      return true;
    }
    else {
      System.err.println("(" + type + ", " + hash + ") is already registered!");
      return false;
    }
  }

  public static void unregisterHandler(String type, long hash) {
    ConcurrentHashMap<Long, Handler> hash_handler = type_hash_handler.get(type);
    hash_handler.remove(hash);
  }
}
