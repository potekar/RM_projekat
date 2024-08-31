package server;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Server_main{
    static ArrayList<MyFile> myFiles = new ArrayList<>();
    static Set<String> users=new HashSet<>();
    static BufferedReader userInfo;
    static PrintWriter notifyUsers;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        int fileId=0;


        while (true) {

            try {
                // Wait for a client to connect and when they do create a socket to communicate with them.
                Socket socket = serverSocket.accept();
                userInfo=new BufferedReader(new InputStreamReader(socket.getInputStream()));

                notifyUsers=new PrintWriter(socket.getOutputStream(),true);

                String username=userInfo.readLine();
                System.out.println(username+" connected!");
                users.add(username);
                notifyUsers.println("djesi hajvanu, trenutno povezanih :" +users.size());


                // Stream to receive data from the client through the socket.
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

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
                        myFiles.add(new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName)));
                        // Increment the fileId for the next file to be received.
                        fileId++;

                        File fileToDownload = new File("RM_grupnjak/src/Database/"+fileName);
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

                        for(MyFile f: myFiles)
                        {
                            System.out.println(f.getName());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyAllusers(String notification)
    {
        System.out.println("Sending notification to all users: "+notification);
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

