import java.io.IOException;
import java.net.*;

public class Client {

	public static void main(String[] args) throws IOException{

		DatagramSocket socket = new DatagramSocket();
		String to_send = "Hello my friend";
		byte[] send_buf = to_send.getBytes();

		InetAddress address = InetAddress.getByName("localhost");

		DatagramPacket dp_send = new DatagramPacket(send_buf, send_buf.length, address, 4400);
		socket.send(dp_send);

		byte[] r_buff = new byte[256];
		dp_send = new DatagramPacket(r_buff, r_buff.length);
		socket.receive(dp_send);

		String received = new String(dp_send.getData());

		System.out.println("Got response this: "+ received);

	}

}
