package UDP;

import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {

    // Couleurs pour la sortie du terminal
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String RESET = "\033[0m";  // Text Reset


    public static final int MAX_BYTES_PACKET = 65000;
    public static final int TIMEOUT = 100;
    public static final int WINDOW = 4;
    public static final double PROBABILITY = 0.15;

    DatagramSocket senderSocket = new DatagramSocket();
    int indexOfPacketToSend = 0;
    InetAddress ipAddressToSend = InetAddress.getByName("localhost");
    int lastAckReceived = -1;
    int lastSeqNumberSent = -1;
    ArrayList<DatagramPacket> packetsAlreadySent = new ArrayList<>();

    public Sender() throws SocketException, UnknownHostException {
    }

    public void send(ArrayList<DatagramPacket> packetlist) throws IOException, InterruptedException {
        System.out.println(CYAN_BOLD + "Sending packets..." + RESET);

        while(true) {

            while((lastSeqNumberSent - lastAckReceived < WINDOW) && (packetsAlreadySent.size() < packetlist.size())) {
                Thread.sleep(25);
                DatagramPacket d = packetlist.get(indexOfPacketToSend);
                lastSeqNumberSent++;
                System.out.println(YELLOW_BOLD + "Sending packet number " + lastSeqNumberSent + "..." + RESET);
                // Probabilité de perdre des paquets
                if(Math.random() > PROBABILITY){
                    senderSocket.send(packetlist.get(indexOfPacketToSend));
                }else{
                    System.out.println(RED_BOLD + "[X] Lost packet with sequence number " + lastSeqNumberSent + RESET);
                }
                packetsAlreadySent.add(packetlist.get(indexOfPacketToSend));

                indexOfPacketToSend++;

            }

            DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

            try{
                senderSocket.setSoTimeout(TIMEOUT);
                System.out.println("Waiting to receive ACK number " + (lastAckReceived + 1) + "...");
                senderSocket.receive(ackPacket);
                lastAckReceived = Utils.byteArrayToInt(ackPacket.getData());
                System.out.println(GREEN_BOLD + "Received ACK number " + lastAckReceived + "." + RESET);

                if(lastAckReceived == packetlist.size() - 1) {
                    System.out.println(GREEN_BOLD + "The last packet was received." + RESET);
                    break;
                }

            } catch (SocketTimeoutException s) {
                System.out.println(RED_BOLD + "Time expired." + RESET);
                for(int i = lastAckReceived + 1; i <= lastSeqNumberSent; i++) {
                    System.out.println(YELLOW_BOLD + "Resending packet number " + i + "..." + RESET);

                    // Send with some probability of loss
                    if(Math.random() > PROBABILITY){
                        senderSocket.send(packetsAlreadySent.get(i));
                    }else{
                        System.out.println(RED_BOLD + "[X] Lost resending of packet with sequence number " + i + RESET);
                    }

                }
            }
        }
        System.out.println(GREEN_BOLD + "All packets sent and received." + RESET);
    }

    public ArrayList<DatagramPacket> splitFile(byte[] file) {
        System.out.println(CYAN_BOLD + "Splitting file into packets..." + RESET);

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
            isLast = (i == nbPackets - 1);
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
