package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientMainManager {
    private static ClientMain cm;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    public static DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public static void setDataInputStream(DataInputStream dataInputStream) {
        ClientMainManager.dataInputStream = dataInputStream;
    }

    public static DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public static void setDataOutputStream(DataOutputStream dataOutputStream) {
        ClientMainManager.dataOutputStream = dataOutputStream;
    }

    public static ClientMain getCm() {
        return cm;
    }

    public static void setCm(ClientMain cM) {
        cm = cM;
    }
}
