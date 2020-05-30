package UDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Application {

    public static void main(String args[]) throws Exception{
        Window app = new Window();

        while(true) {
            Thread.sleep(1);
            if(app.initialized) {
                break;
            }
        }

        //Conditions determinant si on veut partir une fenetre serveur ou client (Sender)

        if(app.side.equals("Sender")) {
            Sender sender = new Sender();
            byte[] fileInBytes = Files.readAllBytes(Paths.get(app.path));
            app.dispose();
            sender.connexionRequest(fileInBytes);

        } else if(app.side.equals("Server")) {
            Server server = new Server();
            app.dispose();
            server.waitForConnexionRequest();
        }
    }
}
