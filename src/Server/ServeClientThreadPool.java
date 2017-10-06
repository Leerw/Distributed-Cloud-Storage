package Server;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/13.
 */
public class ServeClientThreadPool {
    private ArrayList<ServeClientThread> threads = new ArrayList();
    private final int INIT_THREADS = 50;
    private final int MAX_THREADS = 200;
    private Socket client;
    private ClientStrategy ios;



    public ServeClientThreadPool(Socket client, ClientStrategy ios) {
        this.client = this.client;
        this.ios = ios;
        for (int i = 0; i < threads.size(); i++) {
            ServeClientThread t = new ServeClientThread(null, ios);
            t.start();
            threads.add(t);
        }
        try {
            Thread.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void service() {
        ServeClientThread t = null;
        boolean found = false;
        for (int i = 0; i < threads.size(); i++) {
            t = threads.get(i);
            if (t.isIdle()) {
                found = true;
                break;
            }
        }

        if (!found) {
            t = new ServeClientThread(client, ios);
            t.start();
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        t.setClientSocket(client);
    }


}
