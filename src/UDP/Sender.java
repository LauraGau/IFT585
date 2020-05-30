package UDP;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {

    public static final int MAX_BYTES_PACKET = 65000;
    public static final int TIMEOUT = 100;
    public static final int WINDOW = 4;
    public static final double PROBABILITY = 0.15;

    JLabel currentStep;
    JTextArea logHistory;
    JProgressBar progressBar;
    int indexOfPacketToSend = 0;
    InetAddress ipAddressToSend = InetAddress.getByName("localhost");
    int lastAckReceived = -1;
    int lastSeqNumberSent = -1;
    ArrayList<DatagramPacket> packetsAlreadySent = new ArrayList<>();
    DatagramSocket senderSocket = new DatagramSocket();

    public Sender() throws SocketException, UnknownHostException {
    }

    /**
 * Envoit d'une demande de connexion au serveur afin de partir un nouvau thread et d'etablir la connexion
 * @param {byte[]} array d'octets contenant la demande de connexion
 */

    public void connexionRequest(byte[] fileTosplit) throws IOException, InterruptedException {
        byte[] dataRequest = Utils.boolToByteArray(true);
        byte[] dataResponse = new byte[4];
        DatagramPacket conRequest = new DatagramPacket(dataRequest, dataRequest.length, ipAddressToSend, 9876);
        DatagramPacket conResponse = new DatagramPacket(dataResponse, dataResponse.length);
        senderSocket.send(conRequest);
        senderSocket.receive(conResponse);

        send(fileTosplit, Utils.byteArrayToInt(conResponse.getData()));
    }

    /**
 * Envoit du fichier vers le 'receiver'
 * @param {byte[]} fichier a separe en paquets et a envoyer
 * @param {int} port de destination
 */

    public void send(byte[] fileToSplit, int portToUse) throws IOException, InterruptedException {
        ArrayList<DatagramPacket> packetlist = splitFile(fileToSplit, portToUse);
        createGUI(packetlist.size(), portToUse);
        logHistory.append("Sending packets...\n");

        while(true) {

            while((lastSeqNumberSent - lastAckReceived < WINDOW) && (packetsAlreadySent.size() < packetlist.size())) {
                Thread.sleep(20);
                DatagramPacket d = packetlist.get(indexOfPacketToSend);
                lastSeqNumberSent++;
                currentStep.setText("Sending packet number " + lastSeqNumberSent + "...");
                logHistory.append("Sending packet number " + lastSeqNumberSent + "...\n");
                // Probabilité de perdre des paquets
                if(Math.random() > PROBABILITY){
                    senderSocket.send(packetlist.get(indexOfPacketToSend));
                }else{
                    logHistory.append("[X] Lost packet with sequence number " + lastSeqNumberSent + "\n");
                }
                packetsAlreadySent.add(packetlist.get(indexOfPacketToSend));
                indexOfPacketToSend++;
            }

            DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

            try{
                senderSocket.setSoTimeout(TIMEOUT);
                currentStep.setText("Waiting to receive ACK number " + (lastAckReceived + 1) + "...");
                logHistory.append("Waiting to receive ACK number " + (lastAckReceived + 1) + "...\n");
                senderSocket.receive(ackPacket);
                lastAckReceived = Utils.byteArrayToInt(ackPacket.getData());
                currentStep.setText("Received ACK number " + lastAckReceived);
                logHistory.append("Received ACK number  " + lastAckReceived + ".\n");
                progressBar.setValue(lastAckReceived);


                if(lastAckReceived == packetlist.size() - 1) {
                    logHistory.append("The last packet was received.\n");
                    break;
                }

            } catch (SocketTimeoutException s) {
                currentStep.setText("Time expired.");
                logHistory.append("Time expired.\n");
                for(int i = lastAckReceived + 1; i <= lastSeqNumberSent; i++) {
                    currentStep.setText("Resending packet number " + i + "...");
                    logHistory.append("Resending packet number " + i + "...\n");
                    senderSocket.send(packetsAlreadySent.get(i));
                    // Send with some probability of loss
                    if(Math.random() > PROBABILITY){
                        senderSocket.send(packetsAlreadySent.get(i));
                    }else{
                        logHistory.append("[X] Lost resending of packet with sequence number "+ i + "\n");
                    }

                }
            }
        }
        currentStep.setText("All packets sent and received.");
    }

    /**
 * Separation du fichier en paquets de 65000 octets
 * @param {byte[]} fichier a separe en paquets
 * @param {int} port de destination
 */

    public ArrayList<DatagramPacket> splitFile(byte[] file, int portToUse) {
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
            listOfPacketsToSend.add(new DatagramPacket(dataToSend, dataToSend.length, ipAddressToSend, portToUse));
        }
        return listOfPacketsToSend;
    }

    /**
 * Creation du GUI
 * @param {int} nombre de paquets a envoyer
 * @param {int} port de destination
 */

    private void createGUI(int numberOfPackets, int portToUse) {
        JFrame frame = new JFrame("UDP - Sender progress for data sent to port: " + portToUse);
        JPanel mainPanel = new JPanel();
        JProgressBar progressBar = new JProgressBar(0, numberOfPackets - 1);
        JTextArea textArea = new JTextArea("\n",10,30);
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
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

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

        this.logHistory = textArea;
        this.currentStep = label;
        this.progressBar = progressBar;
    }
}
