package UDP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Sender {
    public static final int MAX_BYTES_PACKET = 65000;
    public static final int TIMEOUT = 60;
    public static final int WINDOW = 4;

    DatagramSocket senderSocket = new DatagramSocket();
    int indexOfPacketToSend = 0;
    InetAddress ipAddressToSend = InetAddress.getByName("localhost");
    int lastAckReceived = 0;
    int lastSeqNumberSent = 0;
    ArrayList<DatagramPacket> packetsAlreadySent = new ArrayList<>();

    public Sender() throws SocketException, UnknownHostException {
    }

    public void send(ArrayList<DatagramPacket> packetlist) throws IOException {
        System.out.println("Sending packets...");

        while(true) {

            while(lastSeqNumberSent - lastAckReceived < WINDOW && packetsAlreadySent.size() < packetlist.size()) {
                senderSocket.send(packetlist.get(indexOfPacketToSend));
                packetsAlreadySent.add(packetlist.get(indexOfPacketToSend));
                lastSeqNumberSent++;
                indexOfPacketToSend++;
            }

            DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

            try{
                senderSocket.setSoTimeout(TIMEOUT);
                senderSocket.receive(ackPacket);
                lastAckReceived = Utils.byteArrayToInt(ackPacket.getData());

                /* à modifier avec le numéro du dernier paquet */
                if(lastAckReceived == 0) {
                    break;
                }

            } catch (SocketTimeoutException s) {
                for(int i = lastAckReceived; i < lastSeqNumberSent; i++) {
                    senderSocket.send(packetsAlreadySent.get(i));
                }
            }
        }
        System.out.println("All packets sent and received.");
    }

    public ArrayList<DatagramPacket> splitFile(byte[] file) {
        System.out.println("Splitting file into packets...");

        ArrayList<DatagramPacket> listOfPacketsToSend = new ArrayList<>();
        byte[] packetToSend = new byte[MAX_BYTES_PACKET];
        int seqNumber = 0;
        boolean isLast;

        for(int i = 0; i < file.length; i++) {

            isLast = (i == file.length - 1);
            byte[] isLastInBytes = Utils.boolToByteArray(isLast);
            byte[] seqNumberInBytes = Utils.intToByteArray(seqNumber);
            seqNumber ++;

            // first 4 bytes are for the sequence number
            for(int j = 0; j < 4; j++) {
                packetToSend[j] = seqNumberInBytes[j];
            }

            // 5th byte is for the the last packet
            packetToSend[4] = isLastInBytes[0];

            // rest of bytes if for the data split in the total file
            for(int k = 5; k < MAX_BYTES_PACKET; k++) {
                packetToSend[k] = file[i];
            }
            listOfPacketsToSend.add(new DatagramPacket(packetToSend, packetToSend.length, ipAddressToSend, 9876));
        }
        return listOfPacketsToSend;
    }
}
