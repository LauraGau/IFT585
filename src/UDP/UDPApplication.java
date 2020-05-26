package UDP;

public class UDPApplication {
    public static void main(String args[]) throws Exception{
        if(args[0].equals("R")) {
            UDPServer udpServer = new UDPServer();
            udpServer.waiting();

        } else if(args[0].equals("T")) {
            UDPClient udpClient = new UDPClient();
            String message = "Hello World";

            udpClient.send(message);
        }
    }
}
