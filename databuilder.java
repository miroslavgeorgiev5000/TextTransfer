
import java.io.*;
import java.net.*;
import java.util.*;

public class databuilder{
	ArrayList<datafragment> frags=new ArrayList<datafragment>();
	byte[]data;
	int length;
	public byte[] tobytearr(int a){
		return new byte[] {
			(byte)(a >>> 24),
			(byte)(a >>> 16),
			(byte)(a >>> 8),
            (byte)a};
	}
	public void writeString(String a){
		datafragment asd =new datafragment();
		asd.type=0;
		asd.str=a;
		frags.add(asd);
	}
	public void writeBytes(byte[]b){
		datafragment asd =new datafragment();
		asd.type=3;
		asd.b=b;
		frags.add(asd);
	}

	public byte[] build() throws Exception {
		int len=0;
		for( datafragment v : frags ){
			if(v.type==0){
				len+=v.str.getBytes().length+1;
			}
			if(v.type==3){
				len+=v.b.length;
			}
			if(v.type==1 || v.type==2){
				len+=4;
			}
		}
		length=len;
		byte[]d=new byte[len];
		int count=0;
		for( datafragment v : frags ){
			if(v.type==0){
				byte[]dattmp=v.str.getBytes();
				int lentmp=dattmp.length;
				System.arraycopy(dattmp,
								 0,
								 d,
								 count,
								 lentmp);
				count+=lentmp;
				d[count]=0;
				count++;
			}
			if(v.type==1){

			}
			if(v.type==2){

			}
			if(v.type==3){
				byte[]dattmp=v.b;
				int lentmp=dattmp.length;

				System.arraycopy(dattmp,
								 0,
								 d,
								 count,
								 lentmp);
				count+=lentmp;

			}
		}
		// prefix hash
		byte[] bytelen=tobytearr(d.length);
		byte[] fin = new byte[32+d.length+4];
		byte[] hash = md5.hashByteArr(d).getBytes();
		System.arraycopy(hash,
						 0,
						 fin,
						 0,
						 32);
		System.arraycopy(bytelen,
						 0,
						 fin,
						 32,
						 4);
		System.arraycopy(d,
						 0,
						 fin,
						 36,
						 d.length);

		//dataparser.printbytes(d);


		data=fin;
		return data;
	}
}
class datafragment{
	int type=-1;
	String str; // type 0
	int i;		// type 1
	float f;	// type 2
	byte[]b; // type 3
}