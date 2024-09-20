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
    DataInputStream dataInputStream;
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
        stage.setTitle("Tuborg");
        stage.getIcons().add(new Image("/slike/file.png"));
        stage.setScene(scene);
        stage.show();
        currentStage=stage;

        dataInputStream=controller.getDataInputStream();
        dataOutputStream=controller.getDataOutputStream();
        socket=controller.getSocket();

    }

    public void sendMessage(String message) throws IOException {

        dataOutputStream=ClientMainManager.getDataOutputStream();
        dataOutputStream.writeInt(-69);
        byte[] messageBytes = message.getBytes();
        // Send the length of the name of the file so server knows when to stop reading.
        dataOutputStream.writeInt(messageBytes.length);
        // Send the file name.
        dataOutputStream.write(messageBytes);
    }
    public String getMessage() throws IOException {
        int messageType = dataInputStream.readInt();
        int messageLenght = dataInputStream.readInt();
        byte[] messageBytes = new byte[messageLenght];
        dataInputStream.readFully(messageBytes, 0, messageBytes.length);
        return new String(messageBytes);
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

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void saveFile(String filename, byte[] data)
    {
        Platform.runLater(()->controller.saveFile(filename,data));
    }

    public Socket getSocket() {
        return socket;
    }

    public static void main(String args[]) {

        launch(args);
    }




}
