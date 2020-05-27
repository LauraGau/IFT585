package UDP;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class Application {

    public static void main(String args[]) throws Exception{

        if(args[0].equals("Sender") && args.length == 2) {
            Sender sender = new Sender();
            byte[] fileInBytes = args[1].getBytes();
            ArrayList<DatagramPacket> packetlist = sender.splitFile(fileInBytes);
            sender.send(packetlist);

        } else if(args[0].equals("Receiver")) {
            Receiver receiver = new Receiver();
            receiver.waitAndReceive();
        } else {
            System.out.println("Invalid arguments. 1st argument is either 'Sender' or 'Receiver'. 2nd is the binary file.");
        }

    }
}
