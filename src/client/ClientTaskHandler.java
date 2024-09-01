package client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientTaskHandler extends Thread{

    private ClientMain clientMain;

    private BufferedReader serverReader;

    public ClientTaskHandler(ClientMain clientMain,BufferedReader serverReader) {
        this.clientMain=clientMain;
        this.serverReader=serverReader;
    }


    @Override
    public void run() {
        while (true)
        {
            try {
                String serverOutput=serverReader.readLine();
                System.out.println(serverOutput);
                if(serverOutput.startsWith("~"))
                {
                    System.out.println("fl");
                    Platform.runLater(() -> clientMain.updateFileList(serverOutput));
                }
                else
                {
                    Platform.runLater(() -> clientMain.updateNotification(serverOutput));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
