package client;

public class ClientMainManager {
    private static ClientMain cm;

    public static ClientMain getCm() {
        return cm;
    }

    public static void setCm(ClientMain cM) {
        cm = cM;
    }
}
