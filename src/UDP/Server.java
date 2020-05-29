package UDP;

import java.io.IOException;
import java.net.*;

public class Server {
    DatagramSocket serverSocket = new DatagramSocket(9876);
    private int receiverPort = 60000;

    public Server() throws SocketException {
    }
    
    public void startReceiver() throws SocketException {
        Thread t = new Thread(new Receiver(receiverPort));
        t.start();
        receiverPort++;
    }

    public void sendPortNumber(DatagramPacket connexionRequest) throws IOException {
        byte[] portNumberToUseInBytes = Utils.intToByteArray(receiverPort);
        DatagramPacket connexionResponse = new DatagramPacket(
                portNumberToUseInBytes,
                portNumberToUseInBytes.length,
                connexionRequest.getAddress(),
                connexionRequest.getPort());

        serverSocket.send(connexionResponse);
        startReceiver();
    }

    public void waitForConnexionRequest() throws IOException {
        while(true) {
            byte[] connexionRequest = new byte[1];
            DatagramPacket conRequest = new DatagramPacket(connexionRequest, connexionRequest.length);

            serverSocket.receive(conRequest);

            sendPortNumber(conRequest);
        }
    }


}
