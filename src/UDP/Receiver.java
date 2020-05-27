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
    ArrayList<byte[]> receivedPackets = new ArrayList<>();
    DatagramSocket receiverSocket = new DatagramSocket(9876);


    public Receiver() throws SocketException {
    }

    public void waitAndReceive() throws IOException {
        System.out.println("Receiving packets from the sender.");

        byte[] receivedData = new byte[64995];

        while(true) {
            DatagramPacket currentPacket = new DatagramPacket(receivedData, receivedData.length);
            receiverSocket.receive(currentPacket);

            if(Utils.getPacketSeqNumber(currentPacket) == nextSeqNumber && Utils.byteArrayToBool(currentPacket.getData()[4])){
                nextSeqNumber++;
                receivedPackets.add(currentPacket.getData());
                System.out.println("Last packet.");
                break;

            } else if(Utils.getPacketSeqNumber(currentPacket) == nextSeqNumber) {
                receivedPackets.add(currentPacket.getData());
                System.out.println("Receiving packet " + nextSeqNumber);
                nextSeqNumber++;
            }
            sendAck(currentPacket);

        }
        System.out.println("Received all the packets and sent all the ACKs.");

    }

    public void sendAck(DatagramPacket packet) throws IOException {
        byte[] ackData = Utils.intToByteArray(Utils.getPacketSeqNumber(packet));
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());
        receiverSocket.send(ackPacket);
    }
}
