import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
public class md5{
	public static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}
	public static String hashByteArr(byte[]arr)throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(arr);
		return toHexString(thedigest);
	}
	public static void main(String[]args)throws Exception{
		//System.out.println(hashByteArr(new byte[]{0,1,2,3,4,5,6}));
		System.out.println(hashByteArr(new byte[]{0,1,2,3,4,5,6}));
	}
}