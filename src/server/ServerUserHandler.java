package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class ServerUserHandler extends Thread{

    String username;
    Socket socket;
    ArrayList<MyFile> myFiles;
    Set<ServerUserHandler> users;
    DataInputStream dataInputStream ;
    DataOutputStream dataOutputStream;

    public ServerUserHandler(String username, Socket socket, ArrayList<MyFile> myfiles, Set<ServerUserHandler> users) {
        this.username = username;
        this.socket = socket;
        this.myFiles=myfiles;
        this.users=users;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream=new DataOutputStream(socket.getOutputStream());

            while(true) {
                int messageType=dataInputStream.readInt();
                if(messageType==-69)
                {
                    int messageLenght=dataInputStream.readInt();
                    byte[] messageBytes = new byte[messageLenght];
                    dataInputStream.readFully(messageBytes, 0, messageBytes.length);
                    String message = new String(messageBytes);

                    for(MyFile file:myFiles)
                    {
                        if(file.getName().equals(message))
                        {
                            System.out.println("User "+username+" requested ->"+ file);

                            dataOutputStream.writeInt(file.getName().getBytes().length);
                            dataOutputStream.write(file.getName().getBytes());
                            dataOutputStream.writeInt(file.getData().length);
                            dataOutputStream.write(file.getData());
                            System.out.println("File sent");

                        }
                    }
                }
                else {
                    boolean exist=false;
                    // Read the size of the file name so know when to stop reading.
                    int fileNameLength = messageType;
                    // If the file exists
                    if (fileNameLength > 0) {
                        // Byte array to hold name of file.
                        byte[] fileNameBytes = new byte[fileNameLength];

                        dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                        // Create the file name from the byte array.
                        String fileName = new String(fileNameBytes);
                        // Read from the input stream into the byte array.
                        for (MyFile f : myFiles) {
                            if (fileName.equals(f.getName()))
                            {
                                String message="`File allready in database";
                                exist=true;

                                dataOutputStream.writeInt(-69);
                                byte[] messageBytes = message.getBytes();
                                // Send the length of the name of the file so server knows when to stop reading.
                                dataOutputStream.writeInt(messageBytes.length);
                                // Send the file name.
                                dataOutputStream.write(messageBytes);
                            }
                        }
                        if(!exist)
                        {
                            // Read how much data to expect for the actual content of the file.
                            int fileContentLength = dataInputStream.readInt();
                            // If the file exists.
                            if (fileContentLength > 0) {
                                // Array to hold the file data.
                                byte[] fileContentBytes = new byte[fileContentLength];
                                // Read from the input stream into the fileContentBytes array.
                                dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
                                // Add the new file to the array list which holds all our data.
                                myFiles.add(new MyFile(ServerMain.getFileId(), fileName, fileContentBytes, getFileExtension(fileName)));
                                // Increment the fileId for the next file to be received.
                                ServerMain.setFileId(ServerMain.getFileId()+1);
                                ServerMain.setFileId(ServerMain.getFileId());
                                System.out.println(fileName);

                                File fileToDownload = new File("src/Database/" + fileName);
                                try {
                                    // Create a stream to write data to the file.
                                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);
                                    // Write the actual file data to the file.
                                    fileOutputStream.write(fileContentBytes);
                                    // Close the stream.
                                    fileOutputStream.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                for (MyFile f : myFiles) {
                                    System.out.println(f.getName());
                                }

                                ServerMain.sendFileList();
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.err.println(username+" disconected");
            users.removeIf(u -> username.equals(u.getUsername()));
            ServerMain.notifyAllusers("Current users :"+users.size());

        }

    }

    public DataOutputStream getSendToUser() {
        return dataOutputStream;
    }



    private static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }
}

