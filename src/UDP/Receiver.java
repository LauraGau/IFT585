package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Receiver {

    // Couleurs pour la sortie du terminal
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String RESET = "\033[0m";  // Text Reset

    int nextSeqNumber = 0;
    public static final double PROBABILITY = 0.15;
    ArrayList<DatagramPacket> receivedPackets = new ArrayList<>();
    DatagramSocket receiverSocket = new DatagramSocket(9876);


    public Receiver() throws SocketException {
    }

    public void waitAndReceive() throws IOException {
        while(true) {
            System.out.println(YELLOW_BOLD + "Receiving packets from the sender..." + RESET);
            byte[] receivedData = new byte[65000];
            DatagramPacket currentPacket = new DatagramPacket(receivedData, receivedData.length);
            receiverSocket.receive(currentPacket);

            System.out.println(CYAN_BOLD + "Supposed to receive packet number " + nextSeqNumber + RESET);
            System.out.println(GREEN_BOLD + "Received packet number " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) + RESET);
            if(Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) == nextSeqNumber) {

                if(Utils.isLastPacket(currentPacket)) {
                    receivedPackets.add(currentPacket);
                    System.out.println(GREEN_BOLD + "Last packet." + RESET);
                    sendAck(currentPacket);
                    break;
                }
                else {
                    receivedPackets.add(currentPacket);
                    System.out.println(YELLOW_BOLD + "Sending ACK " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) + "..." + RESET);
                    sendAck(currentPacket);
                    nextSeqNumber++;
                }
            } else if(!receivedPackets.isEmpty()) {
                DatagramPacket lastValidPacketReceived = receivedPackets.get(receivedPackets.size() - 1);
                sendAck(lastValidPacketReceived);
                System.out.println(YELLOW_BOLD + "Sending previous valid ACK " + Utils.byteArrayToInt(receivedPackets.get(receivedPackets.size() - 1).getData()) + "..." + RESET);
            }
        }
        System.out.println(GREEN_BOLD + "Received all the packets and sent all the ACKs." + RESET);
    }

    public void sendAck(DatagramPacket packet) throws IOException {
        byte[] ackData = Utils.getPacketSeqNumberInBytes(packet);
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());

        // Probabilité de perdre des ACK
        if(Math.random() > PROBABILITY){
            receiverSocket.send(ackPacket);
        }else{
            System.out.println(RED_BOLD + "[X] Lost ack with sequence number " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(packet)) + RESET);
        }
    }
}
