import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.sql.*;

public class Server {
Connection db;
final int DELAY = 5;
int serv_port;
DatagramSocket serv_socket;

InetAddress mcast_addr;
int mcast_port;
MulticastSocket mcast_socket;
byte[] mcast_msg;

// java Server <srvc_port> <mcast_addr> <mcast_port>

public static void main(String[] args) {
  if (!argsCorrect(args)) {
    return;
  }

  Server server = new Server(Integer.parseInt(args[0]));

  server.setupMulticast(args[1], Integer.parseInt(args[2]));
  server.setTimer();
  server.recvMsg();
}

private static boolean argsCorrect(String[] args) {
  int size = args.length;

  if (size == 3) {
    if (!Client.isInteger(args[0])) {
      System.err.println("Server port is NaN!");
      return false;
    }
    if (!Client.isInteger(args[2])) {
      System.err.println("Multicast port is NaN!");
      return false;
    }

    return true;
  } else {
    System.err.println("Usage:\njava Server <srvc_port> <mcast_addr> <mcast_port>");
    return false;
  }
}

public Server(int port_number) {
  this.serv_port = port_number;
  this.serv_socket = null;
  try {
    this.serv_socket = new DatagramSocket(this.serv_port);
    this.mcast_msg = (InetAddress.getLocalHost().getHostAddress() + ":" + this.serv_port).getBytes();
  }
  catch (UnknownHostException err) {
    System.err.println("Failed to get local IP!\n " + err.getMessage());
    return;
  }
  catch (SocketException err) {
    System.err.println("Failed to create UDP socket!\n " + err.getMessage());
    return;
  }

  this.mcast_addr = null;
  this.mcast_port = 0;
  this.mcast_socket = null;

  try {
    Class.forName("org.sqlite.JDBC");
    this.db = DriverManager.getConnection("jdbc:sqlite:plates.db");
  }
  catch (Exception err) {
    System.err.println("Failed to connect to database!\n " + err.getClass() + ": " +  err.getMessage());
    System.exit(0);
  }
}

public boolean setupMulticast(String mcast_addr, int mcast_port) {
  this.mcast_port = mcast_port;
  try {
    this.mcast_socket = new MulticastSocket(this.mcast_port);
    this.mcast_addr = InetAddress.getByName(mcast_addr);
  }
  catch (UnknownHostException err) {
    System.err.println("Failed to find IP of '" + mcast_addr + "'\n " + err.getMessage());
    return false;
  }
  catch (IOException err) {
    System.err.println("Failed to create Multicast UDP socket!\n " + err.getMessage());
    return false;
  }

  try {
    this.mcast_socket.joinGroup(this.mcast_addr);
    this.mcast_socket.setTimeToLive(1);
  }
  catch (IOException err) {
    System.err.println("Failed to join multicast group: '" + this.mcast_addr.getHostAddress() + "'\n " + err.getMessage());
    return false;
  }

  return true;
}

public void recvMsg() {
  byte[] buf = new byte[256];
  DatagramPacket packet = new DatagramPacket(buf, buf.length);

  try {
    while (true) {
      this.serv_socket.receive(packet);
      this.processMsg(new String(packet.getData()).trim().toUpperCase(), packet.getAddress(), packet.getPort());
    }
  }
  catch (IOException err) {
    System.err.println("Failed to receive message!\n " + err.getMessage());
    return;
  }
}

private void processMsg(String msg, InetAddress origin, int port) {
  String[] info = msg.split(" ");
  String reply;

  if (info.length < 2) {
    System.out.println("Got faulty message!\n-> '" + msg + "'");
    reply = "-1";
  }
  if (info[0].equals("REGISTER") && info.length >= 3) {
    String plate = info[1];
    String user = "";
    for (int i = 2; i < info.length; i++) {
      user += info[i];
    }
    reply = this.registerPlate(plate, user);
  } else if (info[0].equals("LOOKUP") && info.length == 2) {
    reply = this.lookupPlate(info[1]);
  } else {
    System.out.println("Got faulty message!\n-> '" + msg + "'");
    reply = "-1";
  }

  this.sendMsg(this.serv_socket, reply.getBytes(), origin, port);
}

private String registerPlate(String plate, String user) {
  if (this.insertPlate(plate, user)) {
    return Long.toString(this.numberOfPlates());
  } else {
    return "-1";
  }
}

private String lookupPlate(String plate) {
  String owner;

  if ((owner = this.queryPlate(plate)) != "") {
    return owner + " " + plate;
  } else {
    return "-1";
  }
}

public boolean sendMsg(DatagramSocket socket, byte[] msg, InetAddress to, int port) {
  DatagramPacket packet = new DatagramPacket(msg, msg.length, to, port);

  for (int i = 0; i < 3; i++) {
    try {
      socket.send(packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket: " + err.getMessage() + "\nRetrying in 2 sec...");
      try {
        TimeUnit.SECONDS.sleep(2);
      }
      catch (Throwable err2) {
        System.err.println("Failed to sleep for 2 sec, exiting...");
        return false;
      }
    }
  }

  return false;
}

private boolean insertPlate(String plate, String owner) {
  try {
    Statement stmt = this.db.createStatement();
    String query = "INSERT INTO UserPlate VALUES (\"" + owner + "\", \"" + plate + "\");";

    stmt.executeUpdate(query);
    stmt.close();
    return true;
  }
  catch (SQLException err) {
    System.err.println(err.getCause() + ": " + err.getMessage());
    return false;
  }
}

private String queryPlate(String plate) {
  try {
    Statement stmt = this.db.createStatement();
    String query = "SELECT user FROM UserPlate WHERE UserPlate.plate == \"" + plate + "\"";

    ResultSet res = stmt.executeQuery(query);
    String user = res.getString("user");
    stmt.close();
    return user;
  }
  catch (SQLException err) {
    System.err.println("Failed to query plate!\n " + err.getMessage());
    return "";
  }
}

private long numberOfPlates() {
  try {
    Statement stmt = this.db.createStatement();
    String query = "SELECT count(user) FROM UserPlate";

    ResultSet res = stmt.executeQuery(query);
    int amount = res.getInt("count(user)");
    stmt.close();
    return amount;
  }
  catch (SQLException err) {
    System.err.println("Failed to query plate!\n " + err.getMessage());
    return -1;
  }
}

private boolean sendServInfo() {
  DatagramPacket packet = new DatagramPacket(this.mcast_msg, this.mcast_msg.length, this.mcast_addr, this.mcast_port);

  try {
    this.serv_socket.send(packet);
  }
  catch (IOException err) {
    System.err.println("Failed to send server info!\n " + err.getMessage());
    return false;
  }
  return true;
}

private void setTimer() {
  Server serv = this;
  Runnable task = new Runnable() {
    public void run() {
      serv.sendServInfo();
    }

  };

  ScheduledExecutorService scheduler
    = Executors.newSingleThreadScheduledExecutor();

  scheduler.scheduleAtFixedRate(task, 0, DELAY, TimeUnit.SECONDS);
}

}
