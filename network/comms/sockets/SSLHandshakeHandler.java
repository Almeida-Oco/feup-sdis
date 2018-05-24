package network.comms.sockets;

import java.nio.ByteBuffer;
import java.io.IOException;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult;
import java.util.concurrent.Executors;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult.Status;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

class SSLHandshakeHandler {
  ExecutorService executor = Executors.newSingleThreadExecutor();
  SSLEngine engine;
  SocketChannel socket;
  ByteBuffer my_app_data, my_net_data, peer_app_data, peer_net_data;

  SSLHandshakeHandler(SocketChannel socket, SSLEngine engine, ByteBuffer[] buffers) {
    this.socket        = socket;
    this.engine        = engine;
    this.my_app_data   = buffers[0];
    this.my_net_data   = buffers[1];
    this.peer_app_data = buffers[2];
    this.peer_net_data = buffers[3];
    this.my_net_data.clear();
    this.peer_net_data.clear();
  }

  boolean doHandshake() throws IOException {
    SSLEngineResult engine_res = null;
    HandshakeStatus shake_status;

    while (!this.handshakeFinished((shake_status = this.engine.getHandshakeStatus()))) {
      System.out.println("STATUS = " + shake_status);

      // try {
      //   Thread.sleep(500, 0);
      // }
      // catch (InterruptedException err) {}

      if (shake_status == HandshakeStatus.NEED_UNWRAP) {
        int bytes_read;
        if ((bytes_read = this.socket.read(this.peer_net_data)) < 0) {
          if (this.engine.isInboundDone() && this.engine.isOutboundDone()) {
            System.err.println("Read < 0 data");
            return false;
          }

          try {
            this.engine.closeInbound();
            this.engine.closeOutbound();
          }
          catch (SSLException err) {
            System.err.println("Engine forced to close inbound!\n - " + err.getMessage());
          }
        }
        else {
          try {
            this.peer_net_data.flip();
            engine_res = engine.unwrap(this.peer_net_data, this.peer_app_data);
            this.peer_net_data.compact();
            // this.handleEngineStatus(engine_res.getStatus(), this.peer_app_data, this.peer_net_data, true);
          }
          catch (SSLException err) {
            System.err.println("Problem encountered while processing data!\n - " + err.getMessage());
            err.printStackTrace();
            this.engine.closeOutbound();
          }

          Status status = engine_res.getStatus();
          if (status == Status.OK) {
            System.out.println("OK");
          }
          else if (status == Status.BUFFER_OVERFLOW) {
            System.out.println("  OVERFLOW");
            this.enlargeBuffer(this.peer_app_data, this.engine.getSession().getApplicationBufferSize());
          }
          else if (status == Status.BUFFER_UNDERFLOW) {
            System.out.println("  UNDERFLOW");
            this.peer_net_data.flip();
            this.socket.read(this.peer_net_data);
            engine_res = engine.unwrap(this.peer_net_data, this.peer_app_data);
            this.peer_net_data.compact();
          }
          else if (status == Status.CLOSED) {
            System.out.println("CLOSED!");
            System.exit(2);
          }
        }
      }
      else if (shake_status == HandshakeStatus.NEED_WRAP) {
        this.my_net_data.clear();

        try {
          engine_res = this.engine.wrap(this.my_app_data, this.my_net_data);
          this.my_net_data.flip();

          while (this.socket.write(this.my_net_data) > 0) {
          }
          Status status = engine_res.getStatus();
          if (status == Status.OK) {
            System.out.println("  OK");
          }
          else if (status == Status.BUFFER_OVERFLOW) {
            System.out.println("  OBVERFLOW");
            this.socket.write(this.my_net_data);
            this.my_net_data.compact();
          }
          else if (status == Status.CLOSED) {
            System.out.println("CLOSED!");
            System.exit(2);
          }
          // this.handleEngineStatus(engine_res.getStatus(), this.my_net_data, this.my_app_data, false);
        }
        catch (SSLException err) {
          System.err.println("Problem encountered while processing data!\n - " + err.getMessage());
        }
      }
      else if (shake_status == HandshakeStatus.NEED_TASK) {
        Runnable task;
        while ((task = this.engine.getDelegatedTask()) != null) {
          task.run();
        }
      }
      else {
        System.err.println("Invalid SSL status: " + shake_status);
        return false;
      }
    }

    System.err.println("End of while");
    return this.handshakeFinished(shake_status);
  }

  private boolean handleEngineStatus(Status status, ByteBuffer app_data, ByteBuffer net_data, boolean needs_unwrap) {
    System.out.println("  Engine status = " + status);
    if (status == Status.OK) {
      return true;
    }
    else if (status == Status.BUFFER_OVERFLOW) {
      if (needs_unwrap) {
        app_data = this.enlargeBuffer(app_data, this.engine.getSession().getApplicationBufferSize());
      }
      else {
        net_data = this.enlargeBuffer(net_data, this.engine.getSession().getPacketBufferSize());
      }
      return true;
    }
    else if (status == Status.BUFFER_UNDERFLOW) {
      if (needs_unwrap) {
        // this.socket.read(net_data);
        // net_data = this.shrinkBuffer(net_data);
      }
      else {
        System.err.println("Should not happen!");
        System.exit(1);
      }
      return true;
    }
    else if (status == Status.CLOSED) {
      try {
        this.my_net_data.flip();
        while (this.my_net_data.hasRemaining()) {
          this.socket.write(this.my_net_data);
        }
        this.peer_net_data.clear();
        return true;
      }
      catch (Exception err) {
        System.err.println("Failed to send server close message!\n - " + err.getMessage());
      }
      return false;
    }
    else {
      System.err.println("Wut is this? " + status);
      return false;
    }
  }

  private ByteBuffer shrinkBuffer(ByteBuffer buffer) {
    if (this.engine.getSession().getPacketBufferSize() < buffer.limit()) {
      return buffer;
    }
    else {
      ByteBuffer replacement = this.enlargeBuffer(buffer, this.engine.getSession().getPacketBufferSize());
      buffer.flip();
      replacement.put(buffer);
      return replacement;
    }
  }

  private boolean handshakeFinished(HandshakeStatus status) {
    return status == HandshakeStatus.FINISHED || status == HandshakeStatus.NOT_HANDSHAKING;
  }

  private ByteBuffer enlargeBuffer(ByteBuffer buffer, int proposed_size) {
    if (proposed_size > buffer.capacity()) {
      buffer = ByteBuffer.allocate(proposed_size);
    }
    else {
      buffer = ByteBuffer.allocate(buffer.capacity() * 2);
    }

    return buffer;
  }
}
