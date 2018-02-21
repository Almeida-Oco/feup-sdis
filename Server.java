import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Server{

	int PORT_NUMB;
	String MCAST_ADDR;
	int MCAST_PORT;

	DatagramSocket socket;

	final static String INET_ADDR = "224.0.0.3";

	InetAddress addr;

	//java Server <srvc_port> <mcast_addr> <mcast_port> 

	public static void main(String[] args) throws IOException {

		Server server = new Server(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]) );
	}

	public Server(int port_number, String MCAST_ADDR, int MCAST_PORT) {

		this.PORT_NUMB = port_number;
		this.MCAST_ADDR = MCAST_ADDR;
		this.MCAST_PORT = MCAST_PORT;

		this.socket = setupUDPMulti(MCAST_PORT);

		try{
			((MulticastSocket) this.socket).joinGroup(this.addr);
		}
		catch(IOException io){
			System.out.println("Got IO ex");
			return;
		}
		
		this.recvMsg();
		this.setTimer();

		System.out.println("Set up everything");

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

	private DatagramSocket setupUDPMulti(int port_number) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(port_number);
		}
		catch (SocketException err) {
			System.err.println("Failed to create Multicast UDP socket!\n" + err.getMessage());
			return null;
		}

		try{
	    this.addr = InetAddress.getByName(INET_ADDR);
		}
		catch(UnknownHostException uh){
			return socket;
		}
		return socket;
	}

	private void recvMsg() {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		try {
			while(true) {
				System.out.println("Listening to messages");
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

	private boolean sendMCPort(){

		String msg = Integer.toString(this.MCAST_PORT);

		DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),

		msg.getBytes().length, addr, this.PORT_NUMB);

		try{
			this.socket.send(msgPacket);
			System.out.println("Sendt MC Port");
			return true;
		}
		catch(IOException io){
			return false;
		}
	}

	private boolean setTimer(){

		final Server server_ref = this; 

		Runnable task = new Runnable() {
			public void run() {
				try {
					while (true) {
						server_ref.sendMCPort();
						Thread.sleep(3000L);
					}
				} catch (InterruptedException iex) {}
			}
		};

		ScheduledExecutorService scheduler
                            = Executors.newSingleThreadScheduledExecutor();
 
        int delay = 5;
        scheduler.schedule(task, delay, TimeUnit.SECONDS);
        
		return true;
	}

}
