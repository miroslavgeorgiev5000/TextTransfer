import java.util.*;

class clientcaret{
	public int id=-1;
	public int lastdirection=0;
	public int lastcaretpos=0;
	public int caretpos=0;
	public int markpos=0;
	public int[]color=new int[4];
}
interface timedEvent{
	public void run();
}
class timedEntry{
	long exectime;
	timedEvent event;
	public timedEntry(long time,timedEvent ev){
		exectime=time;
		event=ev;
	}
}
class ResThread extends Thread{
	ArrayList<timedEntry> timeevents = new ArrayList<timedEntry>();
	public void run(){
		while(true){
			try{
				long curtime=System.currentTimeMillis();
				for (Iterator<timedEntry> it = timeevents.iterator(); it.hasNext();) {
					timedEntry tmp=it.next();
					if(tmp.exectime<=curtime){
						tmp.event.run();
						it.remove();
					}

				}
				Thread.currentThread().sleep(33);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}

public class texttransferSERVER{
	public ResThread responseThread = new ResThread();
	HashMap<UDPCLclient,clientcaret> clientcarets = new HashMap<UDPCLclient,clientcaret>();
	String content="test test test test";


	public void delay(long delay,timedEvent ev){
		long curtime=System.currentTimeMillis();
		responseThread.timeevents.add(new timedEntry(delay+curtime,ev));
	}
	public void updateCaret(UDPCLclient cl,int start,int end){
		clientcaret a = clientcarets.get(cl);
		a.lastcaretpos=a.caretpos;
		a.caretpos=start;
		a.markpos=end;
		a.lastdirection=a.caretpos-a.lastcaretpos;
	}
	static public String strInsert(int index,String str,String k){
		String b=str.substring(0,index );
		String c=str.substring(index,str.length());
    	return b+k+c;
	}
	int clientcounter=0;
	public texttransferSERVER() throws Exception{
		responseThread.start();
		delay(2000,new timedEvent(){
			public void run(){
				System.out.println("timed event test 2000 milliseconds");
			}
		});
		final UDPSV srv = new UDPSV(4445);
		srv.addEvent("connect",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				try{
					String received = p.readString();
					System.out.println("client \""+received+"\" connected at: " + new Date().toString());
					clientcaret clinfo = new clientcaret();
					clinfo.id=clientcounter;
					clientcounter++;
					clientcarets.put(cl,clinfo);
					srv.beginMessage("connected");
						srv.writeString("you connected at :"+new Date().toString());
					srv.sendToClient(cl);
					srv.beginMessage("fullbody");
						srv.writeString(content);
					srv.sendToClient(cl);
					srv.beginMessage("fullclients");
						srv.writeString(""+(clientcarets.size()-1));
						for (UDPCLclient key : clientcarets.keySet()) {
							clientcaret tmp = clientcarets.get(key);
							if(key==cl){continue;}
							srv.writeString(tmp.id+"");
							srv.writeString(tmp.caretpos+"");
							srv.writeString(tmp.markpos+"");
						}
					srv.sendToClient(cl);
					srv.beginMessage("clientconnected");
						srv.writeString(clinfo.id+"");
						srv.writeString(clinfo.caretpos+"");
						srv.writeString(clinfo.markpos+"");
					srv.broadcastExcludeClient(cl);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		srv.addEvent("changecaret",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				try{
					updateCaret(cl,Integer.parseInt(p.readString()),Integer.parseInt(p.readString()));

					clientcaret tmp=clientcarets.get(cl);
					srv.beginMessage("clientchangecaret");
						srv.writeString(tmp.id+"");
						srv.writeString(tmp.caretpos+"");
						srv.writeString(tmp.markpos+"");
					srv.broadcastExcludeClient(cl);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		srv.addEvent("keytyped",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				try{

					updateCaret(cl,Integer.parseInt(p.readString()),Integer.parseInt(p.readString()));
					String strtoinsert = p.readString();

					//content=strInsert(a.caretpos,content,strtoinsert);

					clientcaret a = clientcarets.get(cl);
					StringBuilder sb = new StringBuilder(content);
					if(a.caretpos==a.markpos){
						sb.insert(a.caretpos,strtoinsert);
					}else{ //selection
						int s=Math.min(a.caretpos,a.markpos);
						int e=Math.max(a.caretpos,a.markpos);
						for(int k=s;k<e;k++){
							sb.deleteCharAt(s);
						}
						sb.insert(s,strtoinsert);
					}
					content = sb.toString();
					//content.insert(a.caretpos,strtoinsert);
					//a.caretpos+=strtoinsert.length();
					System.out.println("'"+content+"' len: "+content.length());

					clientcaret tmp=clientcarets.get(cl);
					srv.beginMessage("clientkeytyped");
						srv.writeString(tmp.id+"");
						srv.writeString(tmp.caretpos+"");
						srv.writeString(tmp.markpos+"");
						srv.writeString(strtoinsert);
					srv.broadcastExcludeClient(cl);

				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		srv.addEvent("backspacetyped",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				try{
					updateCaret(cl,Integer.parseInt(p.readString()),Integer.parseInt(p.readString()));

					if(content.length()==0){return;}

					clientcaret a = clientcarets.get(cl);
					if(a.caretpos==0&&a.markpos==0){return;}
					StringBuilder sb = new StringBuilder(content);

					System.out.println("a "+a.caretpos+" b "+a.markpos);

					if(a.caretpos==a.markpos){
						sb.deleteCharAt(a.caretpos-1);
					}else{ //selection
						int s=Math.min(a.caretpos,a.markpos);
						int e=Math.max(a.caretpos,a.markpos);
						//int c=e-s;
						for(int k=s;k<e;k++){
							sb.deleteCharAt(s);
						}
					}
					content = sb.toString();
					System.out.println("'"+content+"' len: "+content.length());
					clientcaret tmp=clientcarets.get(cl);
					srv.beginMessage("clientbackspacetyped");
						srv.writeString(tmp.id+"");
						srv.writeString(tmp.caretpos+"");
						srv.writeString(tmp.markpos+"");
					srv.broadcastExcludeClient(cl);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	public static void main(String args[]) throws Exception{
		System.out.println("started at "+ System.currentTimeMillis());
		new texttransferSERVER();
	}

}