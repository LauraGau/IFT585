package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Receiver {
    private int lastAck;
    private List<Integer> ackSent;
    private int waitingSeqNumber;
    DatagramSocket receiverSocket;

    public Receiver() throws SocketException {
        receiverSocket = new DatagramSocket();
    }

    public void receiveAsClient(DatagramPacket packet) throws IOException {
        System.out.println("Received data from server.");

        byte[] receiveData = new byte[packet.getData().length - 5];

        for(int i = 5; i < packet.getData().length; i++) {
            receiveData[i] = packet.getData()[i];
        }

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        receiverSocket.receive(receivePacket);
        receiverSocket.close();
    }

    public void sendAck(int seqNumber, InetAddress ipAdress) throws IOException {
        byte[] seqNumberInBytes = Utils.intToByteArray(seqNumber);
        DatagramPacket packet = new DatagramPacket(seqNumberInBytes, seqNumberInBytes.length, ipAdress, 9876);
        receiverSocket.send(packet);
    }
}
