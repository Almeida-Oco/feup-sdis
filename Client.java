import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class Client {
	String host_name;
	String operator;
	String operand;
	int port_number;
	DatagramSocket socket;


	public static void main(String[] args) throws IOException {
		if (!argsCorrect(args)) {
			return;
		}

		Client client = UDPmulticastClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
		if (!client.sendMsg(client.getMsg())) {
			return;
		}

		String reply = client.recvMsg();
		if (reply != null) {
			System.out.println("GOT: " + reply);
		}
		client.closeSocket();
	}

	private static boolean argsCorrect(String[] args) {
		int size = args.length;
		if (size < 4 || size > 5) {
			System.err.println("Usage:\n  java Client <host_name> <port_number> <oper> <opnd>*");
			return false;
		}
		else if (!isInteger(args[1])) {
			System.err.println("Port number NaN!");
			return false;
		}
		if (size == 5) {
			args[3] += " " + args[4];
		}

		return true;
	}

	private static boolean isInteger(String str) {
		int size = str.length();
		for (int i = 0; i < size; i++) {
			if (!Character.isDigit(str.charAt(i)))
			return false;
		}

		return size > 0;
	}

	public static Client UDPmulticastClient(String host_name, int port_number, String oper, String opnd) {
		Client client = new Client(host_name, port_number, oper, opnd);


		for (int i = 0; i < 2 && client.socket == null; i++) {
			try {
				client.socket = new MulticastSocket();
			}
			catch (IOException err) {
				System.err.println("Failed to create MulticastSocket! Retrying...");
				return null;
			}
		}

		try {
			((MulticastSocket)client.socket).joinGroup(InetAddress.getByName(client.host_name));
			((MulticastSocket)client.socket).setTimeToLive(1);
		}
		catch (IOException err) {
			System.err.println("CEnas");
			return null;
		}
		return client;
	}

	public static Client UDPclient(String host_name, int port_number, String oper, String opnd) {
		Client client = new Client(host_name, port_number, oper, opnd);

		for (int i = 0; i < 2 && client.socket == null; i++) {
			try {
				client.socket = new DatagramSocket();
			}
			catch (IOException exception) {
				System.err.println("Failed to create DatagramSocket! Retrying...");
				return null;
			}
		}
		return client;
	}


	public Client(String host_name, int port_number, String oper, String opnd) {
		this.host_name = host_name;
		this.port_number = port_number;
		this.operator = oper;
		this.operand = opnd;
		this.socket = null;
	}

	public void closeSocket() {
		try {
			((MulticastSocket)this.socket).leaveGroup(InetAddress.getByName(this.host_name));
		}
		catch (IOException err) {
			System.err.println("Failed to leave group!\n " + err.getMessage());
		}
	}

	public String getMsg() {
		return this.operator + " " + this.operand;
	}

	public boolean sendMsg(String msg) {
		byte[] msg_bytes = msg.getBytes();
		boolean sent_message = false;
		InetAddress addr;
		try {
			addr = InetAddress.getByName(this.host_name);
		}
		catch (UnknownHostException err) {
			System.err.println("Failed to get host by name: " + err.getMessage());
			return false;
		}
		DatagramPacket packet = new DatagramPacket(msg_bytes, msg_bytes.length, addr, this.port_number);

		for (int i = 0; i < 3; i++) {
			try {
				System.out.println("WTF!");
				this.socket.send(packet);
				System.out.println("SENT!");
				sent_message = true;
				break;
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

		return sent_message;
	}

	public String recvMsg() {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		boolean received = false;

		for (int i = 0; i < 2 && !received; i++) {
			try {
				this.socket.receive(packet);
				received = true;
			}
			catch (IOException err) {
				System.err.println("Failed to receive message: " + err.getMessage() + "\nRetrying...");
			}
		}

		if (!received) {
			return null;
		}

		return new String(packet.getData(), StandardCharsets.UTF_8);
	}
}
