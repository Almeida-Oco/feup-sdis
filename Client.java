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

		Client client = new Client(args[0], Integer.parseInt(args[1]), args[2], args[3]);
		if (!sendMsg(client.getMsg())) {
			return;
		}

		String reply = client.recvMsg();
		if (reply != NULL) {
			System.out.println("GOT: " + reply);
		}
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


	public Client(String host_name, int port_number, String oper, String opnd) {
		System.out.println(opnd);
		this.host_name = host_name;
		this.port_number = port_number;
		this.operator = oper;
		this.operand = opnd;
		this.socket = null;

		for (int i = 0; i < 2 && this.socket == null; i++) {
			try {
				this.socket = new DatagramSocket();
			}
			catch (SocketException exception) {
				System.err.println("Failed to create DatagramSocket! Retrying...");
			}
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
				this.socket.send(packet);
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
