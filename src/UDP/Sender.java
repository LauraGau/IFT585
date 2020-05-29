package UDP;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {
    public static final int MAX_BYTES_PACKET = 65000;
    public static final int TIMEOUT = 10000;
    public static final int WINDOW = 4;

    JLabel currentStep;
    JTextArea packetDeliveries;
    JProgressBar progressBar;
    int indexOfPacketToSend = 0;
    InetAddress ipAddressToSend = InetAddress.getByName("localhost");
    int lastAckReceived = -1;
    int lastSeqNumberSent = -1;
    ArrayList<DatagramPacket> packetsAlreadySent = new ArrayList<>();
    DatagramSocket senderSocket = new DatagramSocket();

    public Sender() throws SocketException, UnknownHostException {
    }

    public void send(ArrayList<DatagramPacket> packetlist) throws IOException, InterruptedException {

        createGUI(packetlist.size());

        while(true) {

            while((lastSeqNumberSent - lastAckReceived < WINDOW) && (packetsAlreadySent.size() < packetlist.size())) {
                Thread.sleep(20);
                DatagramPacket d = packetlist.get(indexOfPacketToSend);
                senderSocket.send(packetlist.get(indexOfPacketToSend));
                packetsAlreadySent.add(packetlist.get(indexOfPacketToSend));
                lastSeqNumberSent++;
                indexOfPacketToSend++;
                currentStep.setText("Sendind packet number " + lastSeqNumberSent + "...");
            }

            DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

            try{
                senderSocket.setSoTimeout(TIMEOUT);
                currentStep.setText("Waiting to receive ACK number " + (lastAckReceived + 1));
                senderSocket.receive(ackPacket);
                lastAckReceived = Utils.byteArrayToInt(ackPacket.getData());
                currentStep.setText("ACK number " + lastAckReceived + " was received.");
                packetDeliveries.append("Packet number " + lastAckReceived + " was delivered.\n");
                progressBar.setValue(lastAckReceived);

                if(lastAckReceived == packetlist.size() - 1) {
                    System.out.println("The last packet was received.");
                    break;
                }

            } catch (SocketTimeoutException s) {
                currentStep.setText("Time expired.");
                for(int i = lastAckReceived + 1; i <= lastSeqNumberSent; i++) {
                    currentStep.setText("Resending packet number " + i + "...");
                    senderSocket.send(packetsAlreadySent.get(i));
                }
            }
        }
        currentStep.setText("All packets sent and received.");
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

    private void createGUI(int numberOfPackets) {
        JFrame frame = new JFrame("UDP - Sender progress");
        JPanel mainPanel = new JPanel();
        JProgressBar progressBar = new JProgressBar(0, numberOfPackets - 1);
        JTextArea textArea = new JTextArea("Steps\n",10,30);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JLabel label = new JLabel("...", JLabel.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setSize(new Dimension(mainPanel.getPreferredSize().width - 15, 20));
        progressBar.setBackground(Color.WHITE);
        progressBar.setForeground(Color.BLUE);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        textArea.setEnabled(false);
        textArea.setText("");
        textArea.setFont(new Font("Serif", Font.BOLD, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(textArea.getDocument().getLength());

        mainPanel.add(progressBar);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        mainPanel.add(label);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 25)));
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 25)));
        frame.getContentPane().add(mainPanel);

        frame.setLocation(501, 50);
        frame.pack();
        frame.setVisible(true);

        this.packetDeliveries = textArea;
        this.currentStep = label;
        this.progressBar = progressBar;
    }
}
