package Server;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/12.
 */
public class ServeNodesThread extends Thread {

    private NodeStrategy ios = null;
    private DatagramSocket socket = null;

    public String getResult() {
        return result;
    }

    private String result = "Null";


    public ServeNodesThread(NodeStrategy ios) {
        this.ios = ios;
    }

    public synchronized void setSocket(DatagramSocket socket) {
        this.socket = socket;
        notifyAll();
    }

    public boolean isIdle() {
        return socket == null;
    }

    @Override
    public synchronized void run() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            result = ios.service();
            FileServer.writeToLocalInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
