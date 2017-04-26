import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

class clclientinfo{
	public int id=-1;
	public int caretpos=0;
	public int markpos=0;
}
class texttransferGUI implements KeyListener, MouseListener{
	JTextArea ta=new JTextArea("           ", 20, 40);
	MultiCaret c=new MultiCaret();
	TextLineNumber tln = new TextLineNumber(ta);
    JScrollPane scrollPane = new JScrollPane(ta);
	Font font = new Font("Courier New", Font.PLAIN, 14);
	JFrame fr=new JFrame("Multi user coding pad");

	InetAddress address;
	UDPCL a;



	HashMap<Integer,clclientinfo> otherclients = new HashMap<Integer,clclientinfo>();
	Thread constantCaretUpdate;
	int clientcaretstart=0;
	int clientcaretend=0;
	int lastclientcaretstart=0;
	int lastclientcaretend=0;

	void ClientUpdateCaret(int id,int caretpos, int markpos){
		clclientinfo a = otherclients.get(id);
		a.caretpos=caretpos;
		a.markpos=markpos;
	}

	public texttransferGUI(String IPaddr) throws Exception{
		c.setBlinkRate(500);
		//c.setAdditionalDots(Arrays.asList(2,4,7));
		c.setAdditionalDots(otherclients);
		ta.setCaret(c);
		ta.setFont(font);
		ta.addKeyListener(this);
		ta.addMouseListener(this);
		scrollPane.setRowHeaderView( tln );
		fr.add(scrollPane);

		fr.pack();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);

		CaretListener listener = new CaretListener() {
		  public void caretUpdate(CaretEvent caretEvent) {
			try{
				clientcaretstart=caretEvent.getDot();
				clientcaretend=caretEvent.getMark();
				System.out.println(clientcaretstart+" "+clientcaretend);
			}catch(Exception e){
				e.printStackTrace();
			}
		  }
		};

		ta.addCaretListener(listener);

		//address = InetAddress.getByName("localhost");
		address = InetAddress.getByName(IPaddr);
		a = new UDPCL(address,4445);

		a.addEvent("connected",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				String received = p.readString();
				System.out.println(received);
			}
		});
		a.addEvent("fullbody",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				String received = p.readString();
				ta.setText(received);
			}
		});

		a.addEvent("fullclients",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				int clientcount = Integer.parseInt(p.readString());
				for(int k=0;k<clientcount;k++){
					int id=Integer.parseInt(p.readString());
					int caretpos = Integer.parseInt(p.readString());
					int markpos = Integer.parseInt(p.readString());
					clclientinfo a = new clclientinfo();
					a.id=id;
					a.caretpos=caretpos;
					a.markpos=markpos;
					otherclients.put(id,a);
				}
			}
		});
		a.addEvent("clientconnected",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				int id=Integer.parseInt(p.readString());
				int caretpos = Integer.parseInt(p.readString());
				int markpos = Integer.parseInt(p.readString());
				clclientinfo a = new clclientinfo();
				a.id=id;
				a.caretpos=caretpos;
				a.markpos=markpos;
				otherclients.put(id,a);

			}
		});


		a.addEvent("clientchangecaret",new udpevent(){
			public void run(dataparser p,UDPCLclient cl){
				try{
					ClientUpdateCaret(Integer.parseInt(p.readString()),Integer.parseInt(p.readString()),Integer.parseInt(p.readString()));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		a.addEvent("clientkeytyped",new udpevent(){
			public void run(dataparser p,UDPCLclient cli){
				try{
					int cl=Integer.parseInt(p.readString());
					int caretpos=Integer.parseInt(p.readString());
					int markpos=Integer.parseInt(p.readString());
					String strtoinsert = p.readString();
					//content=strInsert(a.caretpos,content,strtoinsert);

					clclientinfo a = otherclients.get(cl);
					//StringBuilder sb = new StringBuilder(content);
					if(a.caretpos==a.markpos){
						ta.insert(strtoinsert,a.caretpos);
					}else{ //selection
						int s=Math.min(a.caretpos,a.markpos);
						int e=Math.max(a.caretpos,a.markpos);
						ta.replaceRange("",s,e);
						ta.insert(strtoinsert,s);
					}
					caretpos+=strtoinsert.length();
					markpos+=strtoinsert.length();
					ClientUpdateCaret(cl,caretpos,markpos);
					//content = sb.toString();
					//content.insert(a.caretpos,strtoinsert);
					//a.caretpos+=strtoinsert.length();
					//System.out.println("'"+content+"' len: "+content.length());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		a.addEvent("clientbackspacetyped",new udpevent(){
			public void run(dataparser p,UDPCLclient cli){
				try{
					int cl=Integer.parseInt(p.readString());
					ClientUpdateCaret(cl,Integer.parseInt(p.readString()),Integer.parseInt(p.readString()));

					if(ta.getText().length()==0){return;}

					clclientinfo a = otherclients.get(cl);
					if(a.caretpos==0&&a.markpos==0){return;}
					//StringBuilder sb = new StringBuilder(content);

					//System.out.println("a "+a.caretpos+" b "+a.markpos);

					if(a.caretpos==a.markpos){
						ta.replaceRange("",a.caretpos-1,a.caretpos);
					}else{ //selection
						int s=Math.min(a.caretpos,a.markpos);
						int e=Math.max(a.caretpos,a.markpos);
						ta.replaceRange("",s,e);
					}
					//content = sb.toString();
					//System.out.println("'"+content+"' len: "+content.length());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		a.beginMessage("connect");
			a.writeString("mynamehere");
		a.sendToServer();


		constantCaretUpdate = new Thread(){
			public void run(){
				while(true){
					try{
						a.beginMessage("changecaret");
							a.writeString(""+clientcaretstart);
							a.writeString(""+clientcaretend);
						a.sendToServer();
						Thread.currentThread().sleep(100);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		constantCaretUpdate.start();
	}
	public void keyTyped(KeyEvent e) {

	}
	boolean isShiftDown(KeyEvent e){
			int modifiersEx = e.getModifiersEx();
			String tmpString = KeyEvent.getModifiersExText(modifiersEx);
			if(tmpString.contains("Shift")){
				return true;
			}
			return false;
	}
	boolean isAltDown(KeyEvent e){
		int modifiersEx = e.getModifiersEx();
		String tmpString = KeyEvent.getModifiersExText(modifiersEx);
		if(tmpString.contains("Alt")){
			return true;
		}
		return false;
	}
	boolean isCtrlDown(KeyEvent e){
		int modifiersEx = e.getModifiersEx();
		String tmpString = KeyEvent.getModifiersExText(modifiersEx);
		if(tmpString.contains("Ctrl")){
			return true;
		}
		return false;
	}
	public void keyPressed(KeyEvent e) {
		String ch=""+e.getKeyChar();

		try{
			if(isAltDown(e) || isCtrlDown(e)){
				return;
			}
			System.out.println("A: "+clientcaretstart);
			if((int)ch.charAt(0)==65535){

					if(isShiftDown(e)){
						return;
					}
				a.beginMessage("changecaret");
					a.writeString(""+clientcaretstart);
					a.writeString(""+clientcaretend);
				a.sendToServer();
			}else if(ch.length()==1 && (int)ch.charAt(0)==8){//backspace
				a.beginMessage("backspacetyped");
					a.writeString(""+clientcaretstart);
					a.writeString(""+clientcaretend);
				a.sendToServer();

				clientcaretstart--;
				clientcaretend--;
				if(clientcaretstart<0){clientcaretstart=0;}
				if(clientcaretend<0){clientcaretend=0;}
			}else{
				if((int)ch.charAt(0)==10 || (int)ch.charAt(0)==9 ){ // tab+shift and enter+shift do nothing
					if(isShiftDown(e)){
						return;
					}
				}
				a.beginMessage("keytyped");
					a.writeString(""+clientcaretstart);
					a.writeString(""+clientcaretend);
					a.writeString(ch);
				a.sendToServer();
			}

		}catch(Exception ee){
			ee.printStackTrace();
		}
	}

	public void keyReleased(KeyEvent e) {
    }
    public static byte[] readFile(String filename) {
		try{
			File initialFile = new File(filename);
			InputStream ts = new FileInputStream(initialFile);
			int s=ts.available();
			byte[] f = new byte[s];
			System.out.println("siz: "+s);
			/*for(int k=0;k<s;k++){
				f[k]=(byte)ts.read();
			}*/
			ts.read(f);
			return f;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
 	public void mousePressed(MouseEvent me) {
    }

	public void mouseExited(MouseEvent event){}
	public void mouseEntered(MouseEvent event){}
	public void mouseReleased(MouseEvent event){
		try{
			clientcaretstart=ta.getCaret().getDot();
			clientcaretend=ta.getCaret().getMark();
			System.out.println("A: "+clientcaretstart);
			a.beginMessage("changecaret");
				a.writeString(""+clientcaretstart);
				a.writeString(""+clientcaretend);
			a.sendToServer();
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	public void mouseClicked(MouseEvent event){}




	public static void main(String args[]) throws Exception{
		new texttransferGUI(new String(readFile("connect_address.txt")));
	}
}

class MultiCaret extends DefaultCaret {
    private HashMap<Integer,clclientinfo> additionalDots;

    public void setAdditionalDots(HashMap<Integer,clclientinfo> additionalDots) {
        this.additionalDots = additionalDots;
    }

    public void paint(Graphics g) {
        super.paint(g);

        try {
            TextUI mapper = getComponent().getUI();

            for (Integer addDot : additionalDots.keySet()) {
				clclientinfo asd=additionalDots.get(addDot);
                Rectangle r = mapper.modelToView(getComponent(), asd.caretpos, getDotBias());

                if(isVisible()) {
                    g.setColor(getComponent().getCaretColor());
                    int paintWidth = 1;
                    r.x -= paintWidth >> 1;
                    g.fillRect(r.x, r.y, paintWidth, r.height);
                }
                else {
                    getComponent().repaint(r);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}