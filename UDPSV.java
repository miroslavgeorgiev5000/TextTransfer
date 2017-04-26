import java.io.*;
import java.net.*;
import java.util.*;

class UDPSV{
	HashMap<String,UDPCLclient> clients;
    protected DatagramSocket socket = null;
    int port;
	EventReceiver recvthr;

	public void createRecvThread() throws Exception {
		if( recvthr != null ){
			recvthr.stop();
			recvthr=null;
		}
		recvthr = new EventReceiver(null,port,socket,true,clients);
	}
    public UDPSV(int p) throws Exception {
		port=p;
        socket = new DatagramSocket(p);
        clients = new HashMap<String,UDPCLclient>();
        createRecvThread();
	}
	public void addEvent(String evname, udpevent e){
		recvthr.addEvent(evname,e);
	}
	databuilder tmpbuild;
	public void beginMessage(String evntname){
		tmpbuild = new databuilder();
		tmpbuild.writeString(evntname);
	}
	public void writeString(String a){
		tmpbuild.writeString(a);
	}
	public void sendToClient(UDPCLclient cl) throws Exception{
		byte[]buf=tmpbuild.build(); //gzip.compress(tmpbuild.build());
		DatagramPacket packet = new DatagramPacket(buf, buf.length, cl.address, cl.port);
		socket.send(packet);
	}
	public void broadcast()throws Exception{
		byte[]buf=tmpbuild.build();
		Iterator it = clients.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			String address = (String)pair.getKey();
			UDPCLclient client = (UDPCLclient)pair.getValue();
			//it.remove(); // avoids a ConcurrentModificationException
			DatagramPacket packet = new DatagramPacket(buf, buf.length, client.address, client.port);
			socket.send(packet);
    	}
	}
	public void broadcastExcludeClient(UDPCLclient cl)throws Exception{
		byte[]buf=tmpbuild.build();
		Iterator it = clients.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			String address = (String)pair.getKey();
			UDPCLclient client = (UDPCLclient)pair.getValue();
			if(cl==client){continue;}
			//it.remove(); // avoids a ConcurrentModificationException
			DatagramPacket packet = new DatagramPacket(buf, buf.length, client.address, client.port);
			socket.send(packet);
    	}
	}
}