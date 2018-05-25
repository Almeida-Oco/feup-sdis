package network.chord;

import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import network.comms.sockets.SSLSocketChannel;


class FingerTableFactory {
  private static final int BIT_NUMBER = 32;
  private static final long MAX_ID    = (long)Math.pow(2, BIT_NUMBER);

  private FingerTableFactory() {
  }

  public static FingerTable newTable(String my_ip, SSLSocketChannel remote_peer) {
    long   my_hash  = FingerTableFactory.hash(my_ip.getBytes());
    String join_msg = FingerTableFactory.joinMessage(my_hash);

    return null;
  }

  public static long hash(byte[] content) {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println(err.getMessage());
      return -1;
    }

    byte[]     cont = intestine.digest(content);
    LongBuffer lb   = ByteBuffer.wrap(cont).order(ByteOrder.BIG_ENDIAN).asLongBuffer();

    return Math.abs(lb.get(1)) % (long)Math.pow(2, BIT_NUMBER);
  }

  private static String joinMessage(long my_hash) {
    String msg = "NEW_PEER [";

    for (int i = 0; i < BIT_NUMBER; i++) {
      msg += (my_hash + (long)Math.pow(2, i)) % MAX_ID + ",";
    }
    msg = msg.substring(0, msg.length() - 1);
    return msg + "]";
  }
}
