package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Receiver {
    int nextSeqNumber = 0;
    ArrayList<DatagramPacket> receivedPackets = new ArrayList<>();
    DatagramSocket receiverSocket = new DatagramSocket(9876);


    public Receiver() throws SocketException {
    }

    public void waitAndReceive() throws IOException {
        while(true) {
            System.out.println("Receiving packets from the sender.");
            byte[] receivedData = new byte[65000];
            DatagramPacket currentPacket = new DatagramPacket(receivedData, receivedData.length);
            receiverSocket.receive(currentPacket);

            System.out.println("Suppose to receive packet number " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)));
            if(Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) == nextSeqNumber) {

                System.out.println("Receiving packet " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)));
                if(Utils.isLastPacket(currentPacket)){
                    receivedPackets.add(currentPacket);
                    System.out.println("Last packet.");
                    sendAck(currentPacket);
                    break;
                }
                else {
                    receivedPackets.add(currentPacket);
                    System.out.println("Continuing receiving packets.");
                    sendAck(currentPacket);
                    nextSeqNumber++;
                }
            } else if(!receivedPackets.isEmpty()) {
                DatagramPacket lastValidPacketReceived = receivedPackets.get(receivedPackets.size() - 1);
                sendAck(lastValidPacketReceived);
                System.out.println("Sending ACK for packet number " + receivedPackets.get(receivedPackets.size() - 1));
            }
        }
        System.out.println("Received all the packets and sent all the ACKs.");
    }

    public void sendAck(DatagramPacket packet) throws IOException {
        byte[] ackData = Utils.getPacketSeqNumberInBytes(packet);
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());
        receiverSocket.send(ackPacket);
    }
}
