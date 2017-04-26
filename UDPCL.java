
import java.io.*;
import java.net.*;
import java.util.*;

class UDPCL{
	DatagramSocket socket = null;
	InetAddress address;
	String straddress;
	int port;
	EventReceiver recvthr;

	public void createRecvThread() throws Exception {
		if( recvthr != null ){
			recvthr.stop();
			recvthr=null;
		}
		recvthr = new EventReceiver(straddress,port,socket,false,null);
	}
	public UDPCL(String addr,int p) throws Exception{
		connect(addr,p);
		createRecvThread();
	}
	public UDPCL(InetAddress addr,int p) throws Exception{
		connect(addr,p);
		createRecvThread();
	}

	public void connect(InetAddress addr, int p) throws Exception{
		if(socket != null){
			socket.close();
			socket=null;
		}
		port=p;
		straddress=addr.getHostAddress();
		address = addr;
		socket=new DatagramSocket();
	}

	public void connect(String addr, int p) throws Exception{
		if(socket != null){
			socket.close();
			socket=null;
		}
		port=p;
		address = InetAddress.getByName(addr);
		straddress=address.getHostAddress();
		socket=new DatagramSocket();
	}

	public void send(String str) throws Exception{
		byte[] buf = str.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
	}
	public void send(byte[] buf) throws Exception{
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
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
	public void writeBytes(byte[]a){
		tmpbuild.writeBytes(a);
	}
	public void sendToServer() throws Exception{
		byte[]buf=tmpbuild.build(); //gzip.compress(tmpbuild.build());
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
	}
	public void close(){
		socket.close();
		socket=null;
		recvthr.stop();
		recvthr=null;
	}
}