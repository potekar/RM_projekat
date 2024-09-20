package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class ServerMain {
    private static ArrayList<MyFile> myFiles = new ArrayList<>();
    private static Set<ServerUserHandler> users=new HashSet<>();
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;
    private static String username;

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

                String action=getMessage();
                if (action.equals("login"))
                {
                    username=getMessage();
                    String password=getMessage();

                    File file = new File("src/server/userDatabase.txt");
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    boolean success=false;
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        String[] userInfo = line.split(" ");
                        if(userInfo[0].equals(username) && userInfo[1].equals(password))
                        {
                            System.out.println(username+" connected!");

                            ServerUserHandler user= new ServerUserHandler(username,socket,myFiles,users);
                            users.add(user);

                            user.start();
                            success=true;
                            break;
                        }
                    }
                    if(success)
                    {
                        sendMessage("1");
                        sendFileList();
                        notifyAllusers("Current users :"+users.size());
                    }
                    else {
                        sendMessage("0");
                    }
                }
                if(action.equals("register"))
                {
                    username=getMessage();
                    String password=getMessage();
                    File file = new File("src/server/userDatabase.txt");

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    FileWriter pw=new FileWriter(file,true);

                    boolean success=false;
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        System.out.println("loop");
                        String[] userInfo = line.split(" ");
                        if(userInfo[0].equals(username))
                        {
                            success=true;
                        }
                    }
                    if(success)
                    {
                        sendMessage("0");
                    }
                    else {
                        pw.write(username+" "+password+"\n");
                        pw.close();
                        sendMessage("1");
                        System.out.println(username+" connected!");
                        ServerUserHandler user= new ServerUserHandler(username,socket,myFiles,users);
                        users.add(user);
                        user.start();
                        sendFileList();
                        notifyAllusers("Current users :"+users.size());
                    }
                }
            } catch (IOException e) {
                System.err.println("User disconnected");
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
        for(Path p:getFilePaths())
        {
            myFiles.add(ServerMain.getFileId(),new MyFile(ServerMain.getFileId(),p.getFileName().toString(),Files.readAllBytes(p),p.getFileName().toString().substring(p.getFileName().toString().lastIndexOf('.')+1)));
            ServerMain.setFileId(ServerMain.getFileId()+1);
        }
    }

    private static List<Path> getFilePaths() throws IOException {

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

    private static List<String> getFileNames() {

        List<String> files=new ArrayList<>();
        DecimalFormat df=new DecimalFormat();
        df.setMaximumFractionDigits(2);

        for(MyFile file:myFiles)
        {
            float fileSize=0;
            String unit="kb";
            if(file.getData().length>1000)
            {
                fileSize= (float) file.getData().length /1000;
                unit="kb";
            }
            if(fileSize>1000)
            {
                fileSize/=1000;
                unit="mb";
            }
            if(fileSize>1000)
            {
                fileSize/=1000;
                unit="gb";
            }

            files.add(file.getName()+" Size:"+df.format(fileSize)+unit);
        }

        return files;
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

