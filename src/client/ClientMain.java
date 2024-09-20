package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import scene.Controller;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClientMain extends Application {
    Socket socket;
    DataOutputStream dataOutputStream;
    BufferedReader serverReader;
    Random random=new Random();
    Controller controller;
    Scene scene;
    PrintWriter sendUserInfo;
    Stage currentStage;

    public void start(Stage stage) throws IOException {
        ClientMainManager.setCm(this);
        controller=new Controller();
        stage.setResizable(false);
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource("/scene/loginScene.fxml"));
        scene=new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/scene/style-scenaPocetna.css").toExternalForm());
        stage.setOnCloseRequest((e)->{
            System.exit(0);
        });
        stage.setTitle("Agency...");
        stage.getIcons().add(new Image("/slike/file.png"));
        stage.setScene(scene);
        stage.show();
        currentStage=stage;

    }

    public void startClient(){
        try
        {
            socket = new Socket("localhost", 1234);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            serverReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //sendUserInfo=new PrintWriter(socket.getOutputStream(),true);
            //sendUserInfo.println(random.nextInt(1,5000));

            sendMessage(Integer.toString(random.nextInt(1,5000)));

            ClientTaskHandler cth=new ClientTaskHandler(socket);
            cth.start();
        }
        catch (IOException e) {
            System.err.println("Server not found");
        }
    }

    private  void sendMessage(String message) throws IOException {
        dataOutputStream.writeInt(-69);
        byte[] messageBytes = message.getBytes();
        // Send the length of the name of the file so server knows when to stop reading.
        dataOutputStream.writeInt(messageBytes.length);
        // Send the file name.
        dataOutputStream.write(messageBytes);
    }

    public void updateUserCount(String notification)
    {
        Platform.runLater(()-> {
            controller.updateUserCount(notification);
        });
    }

    public void updateNotifications(String notification)
    {
        Platform.runLater(()-> {
            controller.updateUserNotification(notification);
        });
    }

    public void updateFileList(String notification)
    {
        Platform.runLater(()->controller.updateFileList(notification));
    }

    public Stage getCurrentStage()
    {
        return currentStage;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void saveFile(String filename,byte[] data)
    {
        Platform.runLater(()->controller.saveFile(filename,data));
    }

    public static void main(String args[]) {

        launch(args);
    }




}
