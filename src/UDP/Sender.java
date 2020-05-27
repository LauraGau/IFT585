package UDP;

import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {
    public static final int MAX_BYTES_PACKET = 65000;
    public static final int TIMEOUT = 10000;
    public static final int WINDOW = 4;

    DatagramSocket senderSocket = new DatagramSocket();
    int indexOfPacketToSend = 0;
    InetAddress ipAddressToSend = InetAddress.getByName("localhost");
    int lastAckReceived = -1;
    int lastSeqNumberSent = -1;
    ArrayList<DatagramPacket> packetsAlreadySent = new ArrayList<>();

    public Sender() throws SocketException, UnknownHostException {
    }

    public void send(ArrayList<DatagramPacket> packetlist) throws IOException, InterruptedException {
        System.out.println("Sending packets...");

        while(true) {

            while((lastSeqNumberSent - lastAckReceived < WINDOW) && (packetsAlreadySent.size() < packetlist.size())) {
                Thread.sleep(50);
                DatagramPacket d = packetlist.get(indexOfPacketToSend);
                senderSocket.send(packetlist.get(indexOfPacketToSend));
                packetsAlreadySent.add(packetlist.get(indexOfPacketToSend));
                lastSeqNumberSent++;
                indexOfPacketToSend++;
                System.out.println("Sendind packet number " + lastSeqNumberSent + "...");
            }

            DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

            try{
                senderSocket.setSoTimeout(TIMEOUT);
                System.out.println("Waiting to receive ACK number " + (lastAckReceived + 1));
                senderSocket.receive(ackPacket);
                lastAckReceived = Utils.byteArrayToInt(ackPacket.getData());
                System.out.println("ACK number " + lastAckReceived + " was received.");

                if(lastAckReceived == packetlist.size() - 1) {
                    System.out.println("The last packet was received.");
                    break;
                }

            } catch (SocketTimeoutException s) {
                System.out.println("Time expired.");
                for(int i = lastAckReceived + 1; i <= lastSeqNumberSent; i++) {
                    System.out.println("Resending packet number " + i + "...");
                    senderSocket.send(packetsAlreadySent.get(i));
                }
            }
        }
        System.out.println("All packets sent and received.");
    }

    public ArrayList<DatagramPacket> splitFile(byte[] file) {
        System.out.println("Splitting file into packets...");

        ArrayList<DatagramPacket> listOfPacketsToSend = new ArrayList<>();
        int seqNumber = 0;
        boolean isLast;
        int splitBeginning = 0;
        int splitEnding = 64995;
        int jump = 64995;

        // Pour tous les paquets qu'on devrait avoir
        int nbPackets = (int) Math.ceil((float)file.length / (float)MAX_BYTES_PACKET);
        for(int i = 0; i < nbPackets; i++) {
            byte[] dataToSend = new byte[MAX_BYTES_PACKET];
            isLast = (i == file.length - 1);
            byte[] isLastInBytes = Utils.boolToByteArray(isLast);
            byte[] seqNumberInBytes = Utils.intToByteArray(seqNumber);
            seqNumber ++;

            // first 4 bytes are for the sequence number
            for(int j = 0; j < 4; j++) {
                dataToSend[j] = seqNumberInBytes[j];
            }

            // 5th byte is for the the last packet
            dataToSend[4] = isLastInBytes[0];

            byte[] dataToCopy = Arrays.copyOfRange(file, splitBeginning, splitEnding);

            // rest of bytes if for the data split in the total file
            for(int k = 0; k < dataToCopy.length; k++) {
                dataToSend[k + 5] = dataToCopy[k];
            }
            splitBeginning += jump;
            splitEnding += jump;
            listOfPacketsToSend.add(new DatagramPacket(dataToSend, dataToSend.length, ipAddressToSend, 9876));
        }
        return listOfPacketsToSend;
    }
}
