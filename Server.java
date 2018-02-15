import java.io.IOException;
import java.net.*;

public class Server {
	public static void main(String[] args) throws IOException {

		int PORT_NUMB = 4400;

		DatagramSocket sock_list = new DatagramSocket(PORT_NUMB);
		System.out.println("Started server on " + Integer.toString(PORT_NUMB));

		byte[] buf = new byte[256];
		DatagramPacket dp_recived = new DatagramPacket(buf, buf.length);

		try {
			while (true) {
				sock_list.receive(dp_recived);
				System.out.println("Received");
				try {
					String str_recive = new String(dp_recived.getData()).trim();
					System.out.println("Hello got this: "+ str_recive);
				} finally {
					System.out.println("Closing the server");
				}
			}
		}
		finally {
			System.out.println("Server closed because errors");
			sock_list.close();
		}

	}

}
