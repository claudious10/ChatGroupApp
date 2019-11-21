/*
    Autors:
    Luis Claudio Soto Ayala A01205935
    Ricardo Rodriguez Garcia A01209849

*/
//This are all the libraries that we will need.
import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import java.net.*; 
import java.io.*; 
import java.util.concurrent.TimeUnit;

public class Server extends JFrame implements ActionListener{
    
    //This are all the variables that we will need and change while the system were running.
	private static final long serialVersionUID = 1L;
	public static String endSesion ="exit";
    public static String unserName = "";
    public static String ip = "";
	public static JTextField text;
	public static JTextField text2;
	public static JTextField text3;
	public static JTextArea area;
	public static JButton b1;
	public static JButton b2;
	public static JButton b3;
	private static int clicked;
	public static JFrame f;
    
	//The next part is to define all the JButtons, JTextField, JTextArea, JFrame and JPanels that we will need for the graphic interface.
    public Server(){
	    clicked = 1;
    	
	    f = new JFrame("Chat");
        f.setSize(600,500);        
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
            
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());   
        
        JPanel p3 = new JPanel();
        p3.setLayout(new BorderLayout());
        
        JPanel p4 = new JPanel();
        p4.setLayout(new BorderLayout());
        
        text = new JTextField("Text here...");
        text.setEnabled(false);
        p1.add(text, BorderLayout.CENTER);
        
        b1=new JButton("Send");
        b1.setEnabled(false);
        p1.add(b1, BorderLayout.EAST); 
        b1.addActionListener(this);
        
        area = new JTextArea();
        area.setEnabled(false);
        JScrollPane sp = new JScrollPane(area);
        p2.add(sp, BorderLayout.CENTER);
        p2.add(p1, BorderLayout.SOUTH);
        
        text2 = new JTextField("Write your name...");
        p3.add(text2, BorderLayout.CENTER);
        
        b2 = new JButton("Accept");
        p3.add(b2, BorderLayout.EAST); 
        b2.addActionListener(this);
        
        text3 = new JTextField("Write the IP between 239.0.0.0 - 239.255.255.255...");
        p4.add(text3, BorderLayout.CENTER);
        
        b3 = new JButton("IP");
        p4.add(b3, BorderLayout.EAST); 
        b3.addActionListener(this);
        
        p4.add(p3, BorderLayout.NORTH);
        p2.add(p4, BorderLayout.NORTH);
        f.setContentPane(p2);
        f.setVisible(true);  
    }
    
    //Volatile to avoid memory error or inconsistency, shared variable.
    static volatile boolean ended=false;
    
    public static void main(String[]args) throws IOException, InterruptedException{
    	//We need to disable IPv6. For that the java documentation indicates setting jvm property java.net.preferIPv4Stack=true and the next will help us to do that.
    	System.setProperty("java.net.preferIPv4Stack" , "true");
    	
    	//Once the program starts, we need to run with javac the 2 files.
    	Runtime.getRuntime().exec("javac Server.java");
    	Runtime.getRuntime().exec("javac Messages.java");
    	
    	//This will start the properties of the JFrame.
        new Server();
        
        int i = 0;
        if(args.length!=1){
            //This is to indicate how the program have to be run it. 
        	System.out.println("Usage: |port:1234|");
        }else{
            
            try{
            	//This while is for wait to the user to put the name and the ip values. 
            	//Once the user put a name and the ip and clicked the corresponding button for each one, it will go out from the while.
            	while(unserName == "" || ip == "") {
            		if(i == 0) {
            			System.out.println(unserName);
            			System.out.println(ip); 
            		}
            		i++;
            	}
            	i = 0;
        		
                //To insert the ip address for the specific group in the object.
                InetAddress grupo = InetAddress.getByName(ip);
                
                //To parse the port into an integer.
                int puerto = Integer.parseInt(args[0]);
                
                //Create the ipc mechanism to comunicate the threads.
                MulticastSocket socket = new MulticastSocket(puerto);
                
                
                //Since we are deploying.
                socket.setTimeToLive(0); 
                //This on localhost only (For a subnet set it as 1) 
                
                //To attach the socket for the specific group address selected for the user.
                socket.joinGroup(grupo); 
                
                //Create the thread and initialize the comunication with that connection.
                Thread user = new Thread(new Messages(socket,grupo,puerto)); 
                
                //start sending and receiving messages
                user.start();
                area.setText(unserName + " the conecction was successfully established with the server!!!\n");
                
                //If the program got out from the While before and the connection success, 
                //the text field, where we will write the message, and the send button will be able to use it.
        		text.setEnabled(true);
        		b1.setEnabled(true);
                
                while(true){ 
                    String mensaje;
                    if(i == 0) {
                    	System.out.println(clicked); 
                    }
                    i++;
                    //This if verify if the flag clicked changed to another value, this means that the button of send was clicked.
                    if(clicked == 0){
                        //To close the session when user type "exit".
                        if(text.getText().equalsIgnoreCase(Server.endSesion)){ 
                            ended = true; 
                            socket.leaveGroup(grupo); 
                            socket.close(); 
                            area.setText(area.getText() + "The conecction was interrupted!!!");
                            text.setEnabled(false);
                            b1.setEnabled(false);
                            //Wait 3 seconds to put the message exit and set disable the text field and button
                            TimeUnit.SECONDS.sleep(3);
                            //After 3 seconds the window will close with the next line
                            f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
                            break; 
                        }
                        
                        //This if check if the text field have something to send, if is empty, it won't send anything.
                        if(text.getText().length() != 0){
                        	
                        	//This obtain what is in the text field of the message and concatenate to the format message with the name of the user.
                        	mensaje = unserName + ": " + text.getText() + "\n";
                        
                        	//Then this set the text field of the message to blank and concatenate to the current data in the text area where are all the messages.
                        	text.setText("");
                        	area.setText(area.getText() + mensaje);
                        
                        	//Create a buffer for the message
                        	byte[] buffer = mensaje.getBytes();
                        
                        	//A datagram socket is the sending or receiving point for a packet delivery service. Each packet sent or received on a datagram socket 
                        	//is individually addressed and routed. Multiple packets sent from one machine to another may be routed differently, and may arrive in any order.
                        	DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,grupo,puerto); 
                        	socket.send(datagram);
                        }
                        //The flag that we use to wait if the button was clicked, we return it to the initial value, waiting for another click.
                        clicked = 1;
                        i = 0;
                    }
                    
                } 
            }catch(SocketException se){ 
                System.out.println("Error: al iniciar comunicacion"); 
                se.printStackTrace(); 
            }catch(IOException ie){ 
                System.out.println("Error: al enviar/recibir mensaje"); 
                ie.printStackTrace(); 
            }
        }
        System.exit(0);
    }
    
    public void actionPerformed(ActionEvent ae) {
    	
    	if(ae.getActionCommand() == "Accept") {
    		unserName = text2.getText(); 
    		text2.setEnabled(false);
    		b2.setEnabled(false);
    	}else if(ae.getActionCommand() == "IP") {
    		ip = text3.getText();
    		text3.setEnabled(false);
    		b3.setEnabled(false);
    	}else{
    		clicked = 0;
    	}
	}

    
}//fin de la clase server