package UDP;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Sender {

    final int WSIZE = 4;
    final int PACKET_LENGTH = 65000;
    InetAddress IPAddress = InetAddress.getByName("localhost");

    public Sender() throws UnknownHostException {
    }

    public void send(ArrayList<DatagramPacket> packetlist) throws IOException {
        System.out.println("Sending packets...");

        DatagramSocket clientSocket = new DatagramSocket();

        for(DatagramPacket packet : packetlist) {
            clientSocket.send(packet);
        }
    }

    public void receiveAck(DatagramPacket packet) {

    }

    public ArrayList<DatagramPacket> splitFile(byte[] file) {
        System.out.println("Splitting file into packets...");
        ArrayList<DatagramPacket> sendPacketList = new ArrayList<>();
        byte[] sendPacket = new byte[PACKET_LENGTH];
        int seqNumber = 0;
        boolean isLast;

        for(int i = 0; i < file.length; i++) {

            isLast = (i == file.length - 1);
            byte[] isLastInBytes = Utils.boolToByteArray(isLast);
            byte[] seqNumberInBytes = Utils.intToByteArray(seqNumber);
            seqNumber ++;

            // first 4 bytes are for the sequence number
            for(int j = 0; j < 4; j++) {
                sendPacket[j] = seqNumberInBytes[j];
            }

            // 5th byte is for the the last packet
            sendPacket[4] = isLastInBytes[0];

            // rest of bytes if for the data split in the total file
            for(int k = 5; k < PACKET_LENGTH; k++) {
                sendPacket[k] = file[i];
            }
            sendPacketList.add(new DatagramPacket(sendPacket, sendPacket.length, IPAddress, 9876));
        }
        return sendPacketList;
    }
}
