package server;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

public class ServerMain {
    private static ArrayList<MyFile> myFiles = new ArrayList<>();
    private static Set<ServerUserHandler> users=new HashSet<>();
    private static BufferedReader userInfo;
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    private static int fileId=0;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started. Listening on port 1234.");
        initializeFileList();
        while (true) {
            try {
                // Wait for a client to connect and when they do create a socket to communicate with them.
                Socket socket = serverSocket.accept();
                dataOutputStream=new DataOutputStream(socket.getOutputStream());
                dataInputStream=new DataInputStream(socket.getInputStream());

                String username=getMessage();
                System.out.println(username+" connected!");

                ServerUserHandler user= new ServerUserHandler(username,socket,myFiles,users);
                users.add(user);

                user.start();
                //sendFile.start();

                sendFileList();
                notifyAllusers("Current users :"+users.size());


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void notifyAllusers(String notification)
    {
        for (ServerUserHandler u:users)
        {
            dataOutputStream = u.getSendToUser();
            sendMessage(notification,dataOutputStream);
            System.out.println("notification sent to user:"+u.username+" -> "+notification);
        }
        System.out.println();
    }

    public static void notifyUser(String username,String notification)
    {
            for (ServerUserHandler u:users)
            {
                if(u.username.toString().equals(username))
                {
                    DataOutputStream p= u.getSendToUser();
                    sendMessage(notification,p);
                    System.out.println("notification sent to user:"+u.username+" -> "+notification);
                }
            }
            System.out.println();
    }

    public static void sendFileList() throws IOException {
        notifyAllusers("~"+getFileNames().toString().substring(1,getFileNames().toString().length()-1));
    }

    public static int getFileId() {
        return fileId;
    }

    public static void setFileId(int fileId) {
        ServerMain.fileId = fileId;
    }

    public static void initializeFileList() throws IOException {
        for(Path p:getFileNames())
        {
            myFiles.add(ServerMain.getFileId(),new MyFile(ServerMain.getFileId(),p.getFileName().toString(),Files.readAllBytes(p),p.getFileName().toString().substring(p.getFileName().toString().lastIndexOf('.')+1)));
            ServerMain.setFileId(ServerMain.getFileId()+1);
        }
    }

    private static List<Path> getFileNames() throws IOException {

        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/Database"))) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encountered during the iteration, the cause is an IOException
            throw ex.getCause();
        }
        return result;

    }


    private static void sendMessage(String message) {
        try {
            dataOutputStream.writeInt(-69);
            byte[] messageBytes = message.getBytes();
            // Send the length of the name of the file so server knows when to stop reading.
            dataOutputStream.writeInt(messageBytes.length);
            // Send the file name.
            dataOutputStream.write(messageBytes);
            }
            catch (IOException e)
            {
                System.err.println("Failed to send message to all users");
            }
        }


    private static void sendMessage(String message,DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeInt(-69);
            byte[] messageBytes = message.getBytes();
            // Send the length of the name of the file so server knows when to stop reading.
            dataOutputStream.writeInt(messageBytes.length);
            // Send the file name.
            dataOutputStream.write(messageBytes);
        }
        catch (IOException e)
        {
            System.err.println("Failed to send message to all users");
        }
    }

    private static String getMessage() throws IOException {
        int messageType=dataInputStream.readInt();
        int messageLenght=dataInputStream.readInt();
        byte[] messageBytes = new byte[messageLenght];
        dataInputStream.readFully(messageBytes, 0, messageBytes.length);
        return new String(messageBytes);
    }

    public static Set<ServerUserHandler> getUsers()
    {
        return users;
    }
}

