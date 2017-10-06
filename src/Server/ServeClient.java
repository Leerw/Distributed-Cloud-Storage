package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/16.
 */
public class ServeClient extends Thread{
    private ServerSocket serverSocket = null;
    private Socket client = null;

    public ServeClient(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                client = serverSocket.accept();
//                new ServeClientThread(client, new ClientStrategy(client)).start();
                new ServeClientThreadPool(client, new ClientStrategy(client)).service();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
