package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Application {

    // arg0 = function(Server, Client); arg1 = file to be sent
    public static void main(String args[]) throws Exception{

        byte[] receivePacket = new byte[PACKET_LENGTH];
        
        //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

        //DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER: " + modifiedSentence);
        clientSocket.close();

        if(args[0].equals("R")) {
            Server server = new Server();
            server.waiting();

        } else if(args[0].equals("T")) {
            Client client = new Client();
            String message = "Hello World";

            client.send(message);
        }
    }
}
