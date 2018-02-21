import java.io.IOException;
import java.net.*;

public class Server {
	public static void main(String[] args) throws IOException {
		int PORT_NUMB = 4400;
		DatagramSocket socket;
		if ((socket = setupUDP(PORT_NUMB)) == null) {
			return;
		}

		

	}

	private static DatagramSocket setupUDP(int port_number) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(port_number);
		}
		catch (SocketException err) {
			System.err.println("Failed to create UDP socket!\n" + err.getMessage());
			return null;
		}

		return socket;
	}

	private static void recvMsg(DatagramSocket socket) {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		try {
			while(true) {
				socket.receive(packet);
				String str_recv = new String(packet.getData()).trim();
				System.out.println("Got this: '" + str_recv + "'");
			}
		}
		catch (IOException err) {
			System.err.println("Failed to receive message!\n " + err.getMessage());
			return;
		}
	}

}
