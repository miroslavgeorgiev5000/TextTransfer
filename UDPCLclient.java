import java.io.*;
import java.net.*;
import java.util.*;
public class UDPCLclient{
	public UDPCLclient(String addr, int p) throws Exception {
		port=p;
		address = InetAddress.getByName(addr);
		straddress=address.getHostAddress();
	}
	InetAddress address;
	String straddress;
	int port;
}