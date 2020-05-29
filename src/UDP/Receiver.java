package UDP;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Receiver implements Runnable {

    int nextSeqNumber = 0;
    public static final double PROBABILITY = 0.15;
    ArrayList<DatagramPacket> receivedPackets = new ArrayList<>();
    DatagramSocket receiverSocket;
    JTextArea logHistory;
    JLabel currentStep;
    JLabel nbPacketsReceivedLabel;
    int port;


    public Receiver(int portToUse) throws SocketException {
        receiverSocket = new DatagramSocket(portToUse);
        port = portToUse;
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
        rebuildFile(receivedPackets);
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

    private void rebuildFile(ArrayList<DatagramPacket> receivedPackets) throws IOException {
        // Temporary new file with extra bytes of the last packet
        String tempPath = System.getProperty("user.dir") + "\\" + "rebuildedFileTemp" + port;
        File rebuildedNewFile = new File(tempPath);
        OutputStream newOs = new FileOutputStream(rebuildedNewFile);

        for(DatagramPacket packet : receivedPackets) {
            byte[] dataToCopy = Arrays.copyOfRange(packet.getData(), 5, 65000);
            newOs.write(dataToCopy);
        }
        newOs.close();

        // Shrinked new file
        String goodPath = System.getProperty("user.dir") + "\\" + "rebuildedFile" + port;
        File rebuildedNewShrinkedFile = new File(goodPath);
        OutputStream newShrinkedOs = new FileOutputStream(rebuildedNewShrinkedFile);
        byte[] newFile = Files.readAllBytes(Paths.get(".\\rebuildedFileTemp" + port));
        int newFileLength = newFile.length;
        int shrinkNewFileLength = 0;

        for(int i = newFileLength - 1; newFile[i] == 0; i--) {
            shrinkNewFileLength = i;
        }

        byte[] dataShrinkedToCopy = Arrays.copyOfRange(newFile, 0, shrinkNewFileLength);
        newShrinkedOs.write(dataShrinkedToCopy);
        newShrinkedOs.close();
        rebuildedNewFile.delete();
    }

    private void createGUI() {
        JFrame frame = new JFrame("UDP - Receiver progress for port: " + port);
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

    @Override
    public void run() {
        try {
            waitAndReceive();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
