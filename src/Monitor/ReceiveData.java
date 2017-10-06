package Monitor;

import Util.FileInfo;
import Util.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/19.
 */
public class ReceiveData implements Runnable{
    private final int myPort = 10000;
    private Socket dataServer;
    private List<FileInfo> fileInfos = new ArrayList<>();
    private List<NodeInfo> nodeInfos = new ArrayList<>();

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }
    @Override
    public void run() {
        try {
            ServerSocket monitorSocket = new ServerSocket(myPort);
            dataServer = monitorSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(dataServer.getInputStream());
            fileInfos = (List<FileInfo>) ois.readObject();
            nodeInfos = (List<NodeInfo>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
