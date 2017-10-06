package Server;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/19.
 */
public class ServeNode extends Thread{
    private int port;
    private DatagramSocket socket = new DatagramSocket();
    private String command = "";
    private String result = "Null";


    public ServeNode(int port, String command) throws SocketException {
        if (port != 0) {
            socket = new DatagramSocket(port);
        }
        this.command = command;
    }

    public String getResult() {
        return this.result;
    }

    @Override
    public void run() {
        try {
            ServeNodesThreadPool nodesThreadPool =  new ServeNodesThreadPool(socket, new NodeStrategy(socket, command));
            nodesThreadPool.service();
            Thread.sleep(300);
            this.result = nodesThreadPool.getResult();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
