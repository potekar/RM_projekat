package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerUserHandler extends Thread{

    String username;
    Socket socket;
    ArrayList<MyFile> myFiles;

    PrintWriter sendToUser;

    public ServerUserHandler(String username, Socket socket, ArrayList<MyFile> myfiles) {
        this.username = username;
        this.socket = socket;
        this.myFiles=myfiles;
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
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            sendToUser =new PrintWriter(socket.getOutputStream(),true);

            while(true) {
                // Read the size of the file name so know when to stop reading.
                int fileNameLength = dataInputStream.readInt();
                // If the file exists
                if (fileNameLength > 0) {
                    // Byte array to hold name of file.
                    byte[] fileNameBytes = new byte[fileNameLength];
                    // Read from the input stream into the byte array.
                    dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                    // Create the file name from the byte array.
                    String fileName = new String(fileNameBytes);
                    // Read how much data to expect for the actual content of the file.
                    int fileContentLength = dataInputStream.readInt();
                    // If the file exists.
                    if (fileContentLength > 0) {
                        // Array to hold the file data.
                        byte[] fileContentBytes = new byte[fileContentLength];
                        // Read from the input stream into the fileContentBytes array.
                        dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
                        // Panel to hold the picture and file name.

                        // Add the new file to the array list which holds all our data.
                        myFiles.add(new MyFile(ServerMain.getFileId(), fileName, fileContentBytes, getFileExtension(fileName)));
                        // Increment the fileId for the next file to be received.
                        ServerMain.setFileId(ServerMain.getFileId() + 1);

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
        catch (IOException e)
        {
            System.err.println(username+" disconected");
        }

    }

    public PrintWriter getSendToUser() {
        return sendToUser;
    }

    private static String getFileExtension(String fileName) {
        // Get the file type by using the last occurence of . (for example aboutMe.txt returns txt).
        // Will have issues with files like myFile.tar.gz.
        int i = fileName.lastIndexOf('.');
        // If there is an extension.
        if (i > 0) {
            // Set the extension to the extension of the filename.
            return fileName.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }
}

