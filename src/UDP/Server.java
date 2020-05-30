package UDP;

import java.io.IOException;
import java.net.*;

public class Server {
    DatagramSocket serverSocket = new DatagramSocket(9876);
    private int receiverPort = 60000;

    public Server() throws SocketException {
    }

    /**
 * Partir le thread qui servira de canal de communication entre le sender et le receiver
 */
    
    public void startReceiver() throws SocketException {
        Thread t = new Thread(new Receiver(receiverPort));
        t.start();
        receiverPort++;
    }

/**
 * Envoit du numero de port a utiliser entre le 'sender' et le 'receiver'
 * @param {DatagramPaquet} paquet contenant le numero de port
 */    

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

    /**
 * Attente de confirmation de connection (numero de port)
 * 
 */

    public void waitForConnexionRequest() throws IOException {
        while(true) {
            byte[] connexionRequest = new byte[1];
            DatagramPacket conRequest = new DatagramPacket(connexionRequest, connexionRequest.length);

            serverSocket.receive(conRequest);

            sendPortNumber(conRequest);
        }
    }


}
