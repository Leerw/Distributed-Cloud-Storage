package Server;

import java.io.IOException;
import java.net.Socket;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/12.
 */
public class ServeClientThread extends Thread {

    private Socket client = null;
    private ClientStrategy ios = null;

    public ServeClientThread(Socket client, ClientStrategy ios) {
        this.client = client;
        this.ios = ios;
    }

    public boolean isIdle() {
        return client == null;
    }

    public synchronized void setClientSocket(Socket client) {
        this.client = client;
        notifyAll();
    }


    @Override
    public synchronized void run() {
        while (true) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                ios.service();
                FileServer.writeToLocalInfo();
                client = null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
