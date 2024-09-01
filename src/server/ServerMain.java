package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

public class ServerMain {
    private static ArrayList<MyFile> myFiles = new ArrayList<>();
    private static Set<ServerUserHandler> users=new HashSet<>();
    private static BufferedReader userInfo;
    private static PrintWriter notifyUsers;

    private static int fileId=0;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);

        while (true) {

            try {
                // Wait for a client to connect and when they do create a socket to communicate with them.
                Socket socket = serverSocket.accept();
                userInfo=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                notifyUsers=new PrintWriter(socket.getOutputStream(),true);

                String username=userInfo.readLine();
                System.out.println(username+" connected!");

                ServerUserHandler user= new ServerUserHandler(username,socket,myFiles);
                user.start();
                users.add(user);

                notifyAllusers("Current users :"+users.size());
                sendFileList();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void notifyAllusers(String notification)
    {
        try {
            for (ServerUserHandler u:users)
            {
                PrintWriter p= u.getSendToUser();
                p.println(notification);
            }
        }
        catch (RuntimeException e){

        }

    }

    public static void sendFileList() throws IOException {
        System.out.println("~"+getFileNames().toString().substring(1,getFileNames().toString().length()-1));
        notifyAllusers("~"+getFileNames().toString().substring(1,getFileNames().toString().length()-1));
    }


    public static int getFileId() {
        return fileId;
    }

    public static void setFileId(int fileId) {
        ServerMain.fileId = fileId;
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
}

