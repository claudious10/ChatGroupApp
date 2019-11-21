/*
    Autors:
    Luis Claudio Soto Ayala A01205935
    Ricardo Rodriguez Garcia A01209849

*/
//This are all the libraries that we will need.
import java.net.*; 
import java.io.*; 

class Messages implements Runnable{ 
	
	//This are all the variables that we will need while the system were running.
    private MulticastSocket socket; 
    private InetAddress grupo; 
    private int puerto; 
    
    //This is the value of the maximum length message. 
    private static final int MAX_LEN_MESSAGE = 300;
    
    //This is the constructure method to initialize the the group with the port, IP address and the socket message.
    Messages(MulticastSocket socket,InetAddress grupo,int puerto){ 
        this.socket = socket; 
        this.grupo = grupo; 
        this.puerto = puerto; 
    } 
    
    public void run(){ 
        //This is the loop to send messages while the connection stills open
        while(!Server.ended){ 
                byte[] buffer = new byte[Messages.MAX_LEN_MESSAGE]; 
                DatagramPacket datagram = new
                DatagramPacket(buffer,buffer.length,grupo,puerto); 
                String message; 
                
            //This try will try to receive the messages that were sending to the group.
            try{ 
                socket.receive(datagram); 
                message = new String(buffer,0,datagram.getLength(),"UTF-8"); 
                if(!message.startsWith(Server.unserName)) 
                	Server.area.setText(Server.area.getText() + message); 
            } 
            catch(IOException e){ 
                System.out.println("Closed session!"); 
            } 
        } 
    } 
}