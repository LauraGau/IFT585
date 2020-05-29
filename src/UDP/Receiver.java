package UDP;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
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
    JTextArea logHistory;
    JLabel currentStep;
    JLabel nbPacketsReceivedLabel;


    public Receiver() throws SocketException {
    }

    public void waitAndReceive() throws IOException, InterruptedException {
        createGUI();

        while(true) {
            currentStep.setText("Receiving packets from the sender...");
            logHistory.append("Receiving packets from the sender...\n");
            byte[] receivedData = new byte[65000];
            DatagramPacket currentPacket = new DatagramPacket(receivedData, receivedData.length);
            receiverSocket.receive(currentPacket);

            currentStep.setText("Supposed to receive packet number " +
                    nextSeqNumber + " and received packet number " +
                    Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)));
            logHistory.append("Supposed to receive packet number " +
                    nextSeqNumber + " and received packet number " +
                    Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) + "\n");

            if(Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) == nextSeqNumber) {
                logHistory.append("Packet number " + nextSeqNumber + " was stored.\n");
                nbPacketsReceivedLabel.setText("Number of packets stored: " + receivedPackets.size());

                if(Utils.isLastPacket(currentPacket)) {
                    receivedPackets.add(currentPacket);
                    sendAck(currentPacket);
                    currentStep.setText("Sending last ACK " + nextSeqNumber);
                    break;
                }
                else {
                    receivedPackets.add(currentPacket);
                    currentStep.setText("Sending ACK" + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) + "...");
                    logHistory.append("Sending ACK" + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) + "...\n");
                    sendAck(currentPacket);
                    nextSeqNumber++;
                }
            } else if(!receivedPackets.isEmpty()) {
                DatagramPacket lastValidPacketReceived = receivedPackets.get(receivedPackets.size() - 1);
                sendAck(lastValidPacketReceived);
                currentStep.setText("Sending previous valid ACK " + Utils.byteArrayToInt(receivedPackets.get(receivedPackets.size() - 1).getData()) + "...");
                logHistory.append("Sending previous valid ACK " + Utils.byteArrayToInt(receivedPackets.get(receivedPackets.size() - 1).getData()) + "...\n");
            }
        }
        currentStep.setText("Received all the packets and sent all the ACKs.");
    }

    public void sendAck(DatagramPacket packet) throws IOException {
        byte[] ackData = Utils.getPacketSeqNumberInBytes(packet);
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());

        // Probabilité de perdre des ACK
        if(Math.random() > PROBABILITY){
            receiverSocket.send(ackPacket);
        }else{
            currentStep.setText("[X] Lost ack with sequence number " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(packet)));
            logHistory.append("[X] Lost ack with sequence number " + Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(packet)) + "\n");
        }
    }

    private void createGUI() {
        JFrame frame = new JFrame("UDP - Receiver progress");
        JPanel mainPanel = new JPanel();
        JTextArea textArea = new JTextArea("Steps\n",10,30);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JLabel label = new JLabel("...", JLabel.CENTER);
        JLabel label2 = new JLabel("Number of packets stored: ");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        textArea.setEnabled(false);
        textArea.setText("");
        textArea.setFont(new Font("Serif", Font.BOLD, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        mainPanel.add(label2);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        mainPanel.add(label);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 25)));
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 25)));
        frame.getContentPane().add(mainPanel);


        frame.setLocation(50, 50);
        frame.pack();
        frame.setVisible(true);

        this.logHistory = textArea;
        this.currentStep = label;
        this.nbPacketsReceivedLabel = label2;
    }
}
