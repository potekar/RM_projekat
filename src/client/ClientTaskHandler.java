package client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import scene.Controller;
import server.ServerUserHandler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.rmi.ServerError;

public class ClientTaskHandler extends Thread{


    private Socket socket;



    public ClientTaskHandler(Socket socket) {
        this.socket=socket;
    }


    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while (true)
            {
                int messageType=dataInputStream.readInt();

                if(messageType==-69)
                {
                    int messageLenght=dataInputStream.readInt();
                    byte[] messageBytes = new byte[messageLenght];
                    dataInputStream.readFully(messageBytes, 0, messageBytes.length);
                    String serverOutput = new String(messageBytes);

                    System.out.println(serverOutput);
                    if(serverOutput.startsWith("~"))
                    {
                        System.out.println("fileList-> "+serverOutput);
                        ClientMainManager.getCm().updateFileList(serverOutput);

                    } else if (serverOutput.startsWith("`")) {
                        System.out.println("Notification- >"+ serverOutput);
                        ClientMainManager.getCm().updateNotifications(serverOutput);
                    } else
                    {
                        System.out.println("User count notification-> "+serverOutput);
                        ClientMainManager.getCm().updateUserCount(serverOutput);
                    }
                }
                else
                {
                    int fileNameLength = messageType;
                    if(fileNameLength>0)
                    {
                        byte[] fileNameBytes = new byte[fileNameLength];
                        dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);

                        int fileLength=dataInputStream.readInt();
                        byte[] fileBytes = new byte[fileLength];
                        dataInputStream.readFully(fileBytes, 0, fileBytes.length);


                        ClientMainManager.getCm().saveFile(new String(fileNameBytes),fileBytes);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Connection to the server lost.");
        }

    }
}
