package Server;

import javax.xml.soap.Node;
import java.net.DatagramSocket;
import java.util.ArrayList;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/13.
 */
public class ServeNodesThreadPool {
    private ArrayList<ServeNodesThread> serveNodesThreads = new ArrayList<>();
    private final int INIT_THREADS = 50;
    private final int MAX_THREADS = 200;
    private NodeStrategy ios = null;
    private DatagramSocket socket = null;
    private String result = "Null";

    public ServeNodesThreadPool(DatagramSocket socket, NodeStrategy ios) {
        this.socket = socket;
        this.ios = ios;
        for (int i = 0; i < INIT_THREADS; i++) {
            ServeNodesThread t = new ServeNodesThread(ios);
            t.start();
            serveNodesThreads.add(t);
        }
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //等待服务器中的ServeNodesThreads都运行
        }
    }

    public String getResult() {
        return this.result;
    }

    public void service() throws InterruptedException {
        ServeNodesThread t = null;
        boolean found = false;
        for (int i = 0; i < serveNodesThreads.size(); i++) {
            t = serveNodesThreads.get(i);
            if (t.isIdle()) {
                found = true;
                break;
            }
        }
        if (!found) {
            t = new ServeNodesThread(ios);
            t.start();
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        t.setSocket(socket);
        Thread.sleep(300);
        this.result = t.getResult();
    }
}
