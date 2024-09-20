package scene;

import client.ClientMain;
import client.ClientMainManager;
import client.ClientTaskHandler;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private Scene scene;
    private Socket socket;
    private static Scene mainScene;
    private Stage stage;
    private File file;
    private FileChooser fil_chooser = new FileChooser();
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
     ClientMain clientMain;

//    ---------------------------FXML---------------------------
    @FXML
    private TextField usernameF;

    @FXML
    private TextField usernameRF;

    @FXML
    private PasswordField passwordF;

    @FXML
    private PasswordField passwordRF;

    @FXML
    private PasswordField passwordRF1;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnGoToRegister;

    @FXML
    private Button btnGoToLogin;

    @FXML
    private Label lblFileName;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblUsers;

    @FXML
    private Label lblNotification;

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnSubmit;

    @FXML
    private Label lblError;

    @FXML
    private ListView<String> lvFileList=new ListView<>();

//    public Controller(ClientMain clientMain) {
//        this.clientMain=clientMain;
//    }

//    ---------------------------Methods---------------------------

    public void login(javafx.event.ActionEvent actionEvent) throws IOException {
        socket = new Socket("localhost", 1234);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream=new DataInputStream(socket.getInputStream());
        ClientMainManager.setDataOutputStream(dataOutputStream);
        ClientMainManager.setDataInputStream(dataInputStream);

        String username=usernameF.getText();
        String password=passwordF.getText();

        sendMessage("login");
        sendMessage(username);
        sendMessage(String.valueOf(password.hashCode()));
        String res=getMessage();
        if(res.equals("1"))
        {
            switchToMain(actionEvent,username);
        }
        else if(res.equals("0")){
            lblError = (Label) ClientMainManager.getCm().getCurrentStage().getScene().lookup("#lblError");
            lblError.setText("Wrong username or password");

        }
    }

    public void sendMessage(String message) throws IOException {
        dataInputStream=ClientMainManager.getDataInputStream();
        dataOutputStream.writeInt(-69);
        byte[] messageBytes = message.getBytes();
        // Send the length of the name of the file so server knows when to stop reading.
        dataOutputStream.writeInt(messageBytes.length);
        // Send the file name.
        dataOutputStream.write(messageBytes);
    }
    public String getMessage() throws IOException {
        dataInputStream=ClientMainManager.getDataInputStream();
        int messageType = dataInputStream.readInt();
        int messageLenght = dataInputStream.readInt();
        byte[] messageBytes = new byte[messageLenght];
        dataInputStream.readFully(messageBytes, 0, messageBytes.length);
        return new String(messageBytes);
    }

    public void register(javafx.event.ActionEvent actionEvent) throws IOException {
        socket = new Socket("localhost", 1234);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream=new DataInputStream(socket.getInputStream());
        ClientMainManager.setDataOutputStream(dataOutputStream);
        ClientMainManager.setDataInputStream(dataInputStream);

        String username=usernameRF.getText();
        String password=passwordRF.getText();
        String password1=passwordRF1.getText();

        if(password.equals(password1) && password.length()>=4 && username.length()>1)
        {
            sendMessage("register");
            sendMessage(username);
            sendMessage(String.valueOf(password.hashCode()));
            String res=getMessage();
            if(res.equals("1"))
            {
                switchToMain(actionEvent,username);
            }
            else if(res.equals("0")){
                lblError = (Label) ClientMainManager.getCm().getCurrentStage().getScene().lookup("#lblError");
                lblError.setText("User already exists");
            }
        }
        else {
            lblError = (Label) ClientMainManager.getCm().getCurrentStage().getScene().lookup("#lblError");
            lblError.setText("Invalid data");
        }

    }
    private Scene getFxmlScene(String name) {
        try {
            return new Scene(FXMLLoader.load(getClass().getResource(name)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void switchToMain(javafx.event.ActionEvent actionEvent,String username) throws IOException {
        stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        scene = getFxmlScene("mainScene.fxml");
        mainScene=getFxmlScene("mainScene.fxml");
        scene.getStylesheets().add(getClass().getResource("/scene/style-prijavljeno.css").toExternalForm());
        stage.setScene(scene);
        mainScene=scene;
        clientMain= ClientMainManager.getCm();
        ClientTaskHandler cth=new ClientTaskHandler();
        cth.start();
        lblUsername = (Label) mainScene.lookup("#lblUsername");
        lblUsername.setText(username);
    }

    public void setBtnGoToRegister(javafx.event.ActionEvent actionEvent) throws IOException {
        stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        scene = getFxmlScene("signUpScene.fxml");
        scene.getStylesheets().add(getClass().getResource("/scene/style-scenaPocetna.css").toExternalForm());
        stage.setScene(scene);
    }

    public void setBtnGoToLogin(javafx.event.ActionEvent actionEvent) throws IOException {
        stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        scene = getFxmlScene("loginScene.fxml");
        scene.getStylesheets().add(getClass().getResource("/scene/style-scenaPocetna.css").toExternalForm());
        stage.setScene(scene);
    }

    public void chooseFile()
    {
        btnBrowse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // get the file selected
                file = fil_chooser.showOpenDialog(stage);

                if (file != null) {

                    lblFileName.setText(file.getAbsolutePath()
                            + "  selected");
                }
            }
        });

    }

    public void sendFile()
    {
        btnSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (file!=null)
                {
                    try {
                        dataOutputStream=ClientMainManager.getDataOutputStream();
                        // Create an input stream into the file you want to send.
                        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                        // Get the name of the file you want to send and store it in filename.
                        String fileName = file.getName();
                        fileName=fileName.replaceAll(" ","_");
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
        });
    }

    public void updateUserCount(String notification){
        lblUsers = (Label) mainScene.lookup("#lblUsers");
        lblUsers.setText(notification);
    }

    public void updateUserNotification(String notification){
        lblNotification = (Label) mainScene.lookup("#lblNotification");
        lblNotification.setText(notification.substring(1));
    }

    public void updateFileList(String rawFileList) {
        lvFileList=(ListView<String>) mainScene.lookup("#lvFileList");
        try {
            lvFileList.getItems().clear();
            rawFileList=rawFileList.substring(1);
            String[] split = rawFileList.split(", ");
            for (String s : split) {
                lvFileList.getItems().add(s);
            }
        } catch (RuntimeException e) {
            System.err.println("empty list");
        }
    }

    public void downloadFile() throws IOException {
        lvFileList=(ListView<String>) mainScene.lookup("#lvFileList");

        lvFileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                if (mouseEvent.getClickCount() == 2) {
                        String currentItemSelected = lvFileList.getSelectionModel().getSelectedItem().toString();
                    try {
                        dataOutputStream=ClientMainManager.getDataOutputStream();
                        dataOutputStream.writeInt(-69);
                        byte[] messageBytes = currentItemSelected.split(" ")[0].getBytes();
                        // Send the length of the name of the file so server knows when to stop reading.
                        dataOutputStream.writeInt(messageBytes.length);
                        // Send the file name.
                        dataOutputStream.write(messageBytes);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void saveFile(String filename,byte[] data){

        FileChooser fileChooser=new FileChooser();

        fileChooser.setInitialFileName(filename);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Downloaded file","*."+getFileExtension(filename)));
        File fileToSave=fileChooser.showSaveDialog(stage);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
            // Write the actual file data to the file.
            fileOutputStream.write(data);
            // Close the stream.
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            System.err.println("save failed");
        }

    }

    private static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }

    public void setLblError(String error)
    {
        lblNotification = (Label) stage.getScene().lookup("#lblError");
        lblNotification.setText(error);
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stage=ClientMainManager.getCm().getCurrentStage();
//        dataOutputStream=ClientMainManager.getCm().getDataOutputStream();
//        dataInputStream=ClientMainManager.getCm().getDataInputStream();
//        socket=ClientMainManager.getCm().getSocket();
        clientMain=ClientMainManager.getCm();
    }
}
