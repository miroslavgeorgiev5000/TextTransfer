import java.io.*;
import java.net.*;
import java.util.*;
class EventReceiver extends Thread{
	HashMap<String,udpevent> events = new HashMap<String,udpevent>();
	HashMap<String,UDPCLclient> clients;
	DatagramSocket socket = null;
	InetAddress address;
	String straddress;
	int port;
	boolean isserver=false;
	public void addEvent(String evname, udpevent e){
			events.put(evname,e);
	}
	public void parseEvent(dataparser p,UDPCLclient cl) throws Exception{
		//byte[] b = (data);
		//dataparser p = new dataparser(b,b.length);
		String eventname = p.readString();
		udpevent event = events.get(eventname);
		if(event != null){
			event.run(p,cl);
		}
	}
	public EventReceiver(String addr,int p,DatagramSocket s,boolean issrv,HashMap<String,UDPCLclient> c) throws Exception{
		socket=s;
		port=p;
		address = InetAddress.getByName(addr);
		straddress=address.getHostAddress();
		isserver=issrv;
		clients=c;

		this.start();
	}
	public void run(){
		while(true){
			try{
				if(socket!=null){
					byte[] buf = new byte[4096];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					byte[] decompresseddata=packet.getData(); //gzip.decompress(packet.getData()).getBytes();
					dataparser p = new dataparser(decompresseddata,packet.getLength());
					if(isserver){
						String packetsrc = packet.getAddress().getHostAddress() + ":" + packet.getPort();
						byte[]dt=p.data;
						if((new String(dt)).startsWith("connect")){
							clients.put(packetsrc,new UDPCLclient(packet.getAddress().getHostAddress(),packet.getPort()));
						}
						UDPCLclient cl = clients.get(packetsrc);
						if(cl != null ){
							parseEvent(p,cl);
						}
					}else{
						if(packet.getAddress().getHostAddress().equals(straddress) && packet.getPort()==port){
							parseEvent(p,null);
						}
					}
				}
			}catch(IOException e){

			}catch(Exception ee){
				ee.printStackTrace();
			}
			/*try{
				Thread.currentThread().sleep(20);
			}catch(Exception e){
				e.printStackTrace();
			}*/
		}
	}

}