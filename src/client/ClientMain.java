package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class ClientMain extends Application {

    File file;
    Socket socket;
    DataOutputStream dataOutputStream;
    BufferedReader serverReader;
    Label notifications;
    ListView<String> fileList;

    Random random=new Random();

    public void start(Stage stage)
    {
        try{
            socket = new Socket("localhost", 1234);
            // Create an output stream to write to the server over the socket connection.
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            serverReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter sendUserInfo=new PrintWriter(socket.getOutputStream(),true);
            sendUserInfo.println(random.nextInt(1,5000));

        } catch (UnknownHostException e) {
            System.err.println("Server not found");
        } catch (IOException e) {
            System.err.println("Server not found");
        }

        try {
            stage.setTitle("FileChooser");

            FileChooser fil_chooser = new FileChooser();

            Label label = new Label("no files selected");

            notifications=new Label(serverReader.readLine());

            Button btn_open = new Button("Show open dialog");
            Button btn_send=new Button("Send file");

            fileList =new ListView<>();
//            String[] split=serverReader.readLine().split(", ");
//            for(String s:split)
//            {
//                s=s.substring(13);
//                fileList.getItems().add(s);
//            }


            EventHandler<ActionEvent> openFile = new EventHandler<>() {

                        public void handle(ActionEvent e) {

                            // get the file selected
                            file = fil_chooser.showOpenDialog(stage);

                            if (file != null) {

                                label.setText(file.getAbsolutePath()
                                        + "  selected");
                            }
                        }
                    };

            EventHandler<ActionEvent> sendFile= new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent actionEvent) {
                            if (file!=null)
                            {
                                try {
                                    // Create an input stream into the file you want to send.
                                    FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                                    // Get the name of the file you want to send and store it in filename.
                                    String fileName = file.getName();
                                    // Convert the name of the file into an array of bytes to be sent to the server.
                                    byte[] fileNameBytes = fileName.getBytes();
                                    // Create a byte array the size of the file so don't send too little or too much data to the server.
                                    byte[] fileBytes = new byte[(int)file.length()];
                                    // Put the contents of the file into the array of bytes to be sent so these bytes can be sent to the server.
                                    fileInputStream.read(fileBytes);
                                    // Send the length of the name of the file so server knows when to stop reading.
                                    dataOutputStream.writeInt(fileNameBytes.length);
                                    // Send the file name.
                                    dataOutputStream.write(fileNameBytes);
                                    // Send the length of the byte array so the server knows when to stop reading.
                                    dataOutputStream.writeInt(fileBytes.length);
                                    // Send the actual file.
                                    dataOutputStream.write(fileBytes);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            else{
                                System.out.println("no file selected");
                            }
                        }
                    };

            btn_open.setOnAction(openFile);
            btn_send.setOnAction(sendFile);
            VBox vbox = new VBox(30, fileList,notifications,label, btn_open,btn_send);

            vbox.setAlignment(Pos.CENTER);

            Scene scene = new Scene(vbox, 800, 500);

            stage.setScene(scene);

            stage.show();

            ClientTaskHandler cth=new ClientTaskHandler(this,serverReader);
            cth.start();
        }

        catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }

    public void updateNotification(String notification)
    {
            notifications.setText(notification);
    }

    public void updateFileList(String rawFileList)
    {
        try {
            fileList.getItems().clear();
            String[] split=rawFileList.split(", ");
            for(String s:split)
            {
                s=s.substring(13);
                fileList.getItems().add(s);
            }
        }
        catch (RuntimeException e)
        {
            System.err.println("empty list");
        }

    }


    public static void main(String args[])
    {

        launch(args);
    }
}
