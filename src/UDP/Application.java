package UDP;

import java.io.File;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Application {
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String RESET = "\033[0m";  // Text Reset

    public static void main(String args[]) throws Exception{

        if(args[0].equals("Sender") && args.length == 2) {
            Sender sender = new Sender();

            byte[] fileInBytes = Files.readAllBytes(Paths.get(args[1]));
            System.out.println(CYAN_BOLD + "File is " + fileInBytes.length + " bytes." + RESET);

            ArrayList<DatagramPacket> packetlist = sender.splitFile(fileInBytes);
            System.out.println(CYAN_BOLD + "File is split in " + packetlist.size() + " packets." + RESET);

            sender.send(packetlist);

        } else if(args[0].equals("Receiver")) {
            Receiver receiver = new Receiver();
            receiver.waitAndReceive();
        } else {
            System.out.println(RED_BOLD + "Invalid arguments. 1st argument is either 'Sender' or 'Receiver'. 2nd is the binary file." + RESET);
        }

    }
}
