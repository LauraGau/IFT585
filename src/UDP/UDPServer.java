package UDP;

import java.net.*;

class UDPServer {
    private int port = 9876;
    private byte[] receiveData;
    private byte[] sendData;
    private DatagramSocket serverSocket;

    public UDPServer() throws SocketException {
        serverSocket = new DatagramSocket(port);
        receiveData = new byte[1024];
        sendData = new byte[1024];
    }

    public void waiting() throws Exception {

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String( receivePacket.getData());
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}
