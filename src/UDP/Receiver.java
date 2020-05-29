package UDP;

import javax.swing.*;
import java.awt.*;
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
    JTextArea packetsStored;
    JLabel currentStep;
    JLabel nbPacketsReceivedLabel;


    public Receiver() throws SocketException {
    }

    public void waitAndReceive() throws IOException {
        createGUI();

        while(true) {
            currentStep.setText("Waiting to receive packets from the sender.");
            byte[] receivedData = new byte[65000];
            DatagramPacket currentPacket = new DatagramPacket(receivedData, receivedData.length);
            receiverSocket.receive(currentPacket);

            currentStep.setText("Suppose to receive packet number " +
                    nextSeqNumber + " and received packet number " +
                    Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)));

            if(Utils.byteArrayToInt(Utils.getPacketSeqNumberInBytes(currentPacket)) == nextSeqNumber) {
                packetsStored.append("Packet number " + nextSeqNumber + " was stored.\n");
                nbPacketsReceivedLabel.setText("Number of packets stored: " + receivedPackets.size());

                if(Utils.isLastPacket(currentPacket)) {
                    receivedPackets.add(currentPacket);
                    sendAck(currentPacket);
                    currentStep.setText("Sending last ACK " + nextSeqNumber);
                    break;
                }
                else {
                    receivedPackets.add(currentPacket);
                    currentStep.setText("Sending ACK" + nextSeqNumber);
                    sendAck(currentPacket);
                    nextSeqNumber++;
                }
            } else if(!receivedPackets.isEmpty()) {
                DatagramPacket lastValidPacketReceived = receivedPackets.get(receivedPackets.size() - 1);
                sendAck(lastValidPacketReceived);
                currentStep.setText("Sending previous valid ACK " + Utils.byteArrayToInt(receivedPackets.get(receivedPackets.size() - 1).getData()));
            }
        }
        currentStep.setText("Received all the packets and sent all the ACKs.");
    }

    public void sendAck(DatagramPacket packet) throws IOException {
        byte[] ackData = Utils.getPacketSeqNumberInBytes(packet);
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, packet.getAddress(), packet.getPort());
        receiverSocket.send(ackPacket);
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
        textArea.setCaretPosition(textArea.getDocument().getLength());

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

        this.packetsStored = textArea;
        this.currentStep = label;
        this.nbPacketsReceivedLabel = label2;
    }
}
