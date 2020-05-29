package UDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Application {

    public static void main(String args[]) throws Exception{
        JFrame frame = new JFrame("User Datagram Protocol");
        Window app = new Window();

        while(true) {
            if(app.initialized) {
                break;
            }
        }

        if(app.side.equals("Sender")) {
            Sender sender = new Sender();
            byte[] fileInBytes = Files.readAllBytes(Paths.get(app.path));
            ArrayList<DatagramPacket> packetlist = sender.splitFile(fileInBytes);
            app.dispose();
            sender.send(packetlist);

        } else if(app.side.equals("Receiver")) {
            app.dispose();
            Receiver receiver = new Receiver();
            receiver.waitAndReceive();

        } else {
            System.out.println("Invalid arguments. 1st argument is either 'Sender' or 'Receiver'. 2nd is the binary file.");
        }
    }


}
