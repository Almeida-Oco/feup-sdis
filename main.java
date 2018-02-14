	import java.io.IOException;
	import java.net.*;


	public class main {
		
		public static void main(String[] args) throws IOException{

			if(args.length < 1) {
				System.out.println("Usage: java test server || java test <client> <what>");
				return;
			}

			if( args[0].equals("server")) {

				DatagramSocket sock_list = new DatagramSocket(4400);
				System.out.println("Started server on 4400");

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

			} else {

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

	}
