

import java.io.*;
import java.net.*;
import java.util.*;



public class dataparser{
	public static void printbytes(String a){
		byte[]b=a.getBytes();
		System.out.print("String: ");
		for(int k=0;k<b.length;k++){
			System.out.print(b[k]+", ");
		}
		System.out.println();
	}
	public static void printbytes(byte[]a){
		System.out.print("byte[]: ");
		for(int k=0;k<a.length;k++){
			System.out.print(a[k]+", ");
		}
		System.out.println();
	}
	public int fromByteArray(byte[] bytes) {
		 return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
	byte[]data;
	String hash;
	int len;
	int pos=0;
	public dataparser(byte[]d,int l) throws Exception {
		byte[]a=new byte[32];
		System.arraycopy(
					d,
					0,
					a,
					0,
					32);
		hash = new String(a);
		byte[] bytelen = new byte[4];
		System.arraycopy(
					d,
					32,
					bytelen,
					0,
					4);
		int ddlen=fromByteArray(bytelen);
		byte[] dd = new byte[ddlen];
		System.arraycopy(
					d,
					36,
					dd,
					0,
					ddlen);

		//printbytes(dd);
		data=dd;
		len=ddlen;
		pos=0;

		if(md5.hashByteArr(data).equals(hash)){
			//System.out.println("hash matches");
		}else{
			System.out.println("hash does not match");
			System.exit(0);
		}
	}

	public String readString(){
		int startpos=pos;
		while(data[pos] != 0){
			pos++;
		}
		int strlen=pos-startpos;
		pos++;
		if(strlen==0){
			return "";
		}
		byte[] ret=new byte[strlen];
		System.arraycopy(data,
		                 startpos,
		                 ret,
		                 0,
                 strlen);
        //printbytes(ret);
		return new String(ret, 0, strlen);
	}
	public byte[] readBytes(int l){
		int startpos=pos;
		int strlen=l;
		pos+=l;
		if(strlen==0){
			return new byte[0];
		}
		byte[] ret=new byte[strlen];
		System.arraycopy(data,
						startpos,
						ret,
						0,
						strlen);
        //printbytes(ret);
		return ret;
	}
}