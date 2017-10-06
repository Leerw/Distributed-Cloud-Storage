package Server;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/19.
 */
public class CommunicateMonitor implements Runnable{

    private String monitorIp;
    private int monitorPort;
    private Socket socket;

    public CommunicateMonitor(String monitorIp, int monitorPort) {
        this.monitorIp = monitorIp;
        this.monitorPort = monitorPort;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(monitorIp, monitorPort);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(FileServer.getFileSet());
            oos.writeObject(FileServer.getNodeSet());
            oos.flush();
            oos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
