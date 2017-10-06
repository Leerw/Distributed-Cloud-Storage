package Server;


import Util.FileInfo;
import Util.NodeInfo;
import StorageNode.StorageNode;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lee_rw on 2017/7/11.
 * 一个FileServer应用（暂时不考虑多个FileServer应用），提供文件存储节点StorageNode的管理功能，提供文件的管理功能。
 * FileServer应用提供文件存储节点的管理。提供存储文件的管理功能。
 * FileClient在进行文件上传和下载时，必须通过FileServer来获取文件存储在哪个节点，哪个备份节点的一些文件信息。
 * FileServer管理每个文件都需要通过文件编号来完成，在其内存中通过Map集合来管理所有文件。
 * 每个文件都需要在两个节点上进行1+1备份存储。FileServer分配存储节点需要考虑负载均衡。
 * 上传成功后，FileServer需要将文件编号信息传递给客户端，客户端以后通过该文件编号进行文件下载，文件删除。
 * 在FileServer管理所有文件信息。文件编号采用UUID, UUID.randomUUID().toString()
 * 在FileServer中需要管理所有存储节点信息。
 * FileServer在内存中需要管理后端FileStorage服务器的信息，包括名称，ip，端口，
 * 容量，实际容量，剩余容量，文件数量，是否可用等信息。
 * 文件信息包括：编号，文件原始名称，文件大小，主存储节点信息，备份节点信息，等等。
 * 这些信息建议使用集合来进行存储，同时要求支持序列化到文件中。在FileServer启动后，需要读取这些序列化信息，服务器退出时，需要
 * 保存这些序列化信息到文件中。
 */
public class FileServer {

    private static final int clientPort = 6000;       //服务器接收客户端的端口
    private static final int storageNodePort = 7000;  //服务器接收所有结点的注册信息
    private static final int confirmDownloadedPort = 8000;
    private static final int monitorPort = 10000;
    private static final String monitorIp = "192.168.1.1";


    private ServerSocket serveClientSocket;
    private ServerSocket serveNodeSocket;
    public static Map<NodeInfo, Boolean> isOnline = new HashMap<>();       //存储StorageNodes的在线信息
    private static List<NodeInfo> nodeSet = new ArrayList<>();
    private static List<FileInfo> fileSet = new ArrayList<>();
    private static List<String> userNames = new ArrayList<>();
    private static Map<String, List<FileInfo>> userFiles = new HashMap<>();
    public static Map<FileNum, NodeInfo> uuidNode = new HashMap<>();
    public static File userNameLocal = new File("userNameLocal.obj");
    public static File uuidNodeLocal = new File("uuidNodeLocal.obj");
    public static File nodesLocal = new File("nodesLocal.obj");
    public static File fileLocal = new File("fileLocal.obj");
    public static File userFileLocal = new File("userFileLocal.obj");

    public FileServer(ServerSocket serverSocket, ServerSocket serveNodeSocket) {
        this.serveClientSocket = serverSocket;
        this.serveNodeSocket = serveNodeSocket;
    }

    public static void main(String[] args) throws IOException {
        //FileServer fs = new FileServer(new ServerSocket(clientPort), new ServerSocket(storageNodePort));
        System.out.println("Server is ready to provide service");
        getLocalInfo();
        for (String userName :
                userNames) {
            System.out.println("用户： " + userName);
        }
/*
        CommunicateMonitor startMonitor = new CommunicateMonitor(monitorIp, monitorPort);
        ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();
        monitorService.scheduleAtFixedRate(startMonitor,10,100,TimeUnit.MILLISECONDS);
        */

        //new ServeNodesThread(new NodeStrategy(storageNodePort,"0")).start();

        Runnable runnable = () -> {
            //判断有没有结点掉线
            Iterator<Map.Entry<NodeInfo, Boolean>> entries = isOnline.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<NodeInfo, Boolean> entry = entries.next();
                if (entry.getValue().equals(false)) {
                    isOnline.remove(entry.getKey());
                }
            }
        };
        ScheduledExecutorService checkService = Executors.newSingleThreadScheduledExecutor();
        checkService.scheduleAtFixedRate(runnable, 10, 10000, TimeUnit.MILLISECONDS);

        new ServeClient(clientPort).start();
        //开启一个默认的服务结点线程，也就是接收来自Node的注册信息
//        new ServeNodesThread(new NodeStrategy(storageNodePort,"")).start();
//        new ServeNodesThread(new NodeStrategy(storageNodePort + 1, "")).start();
        new ServeNode(storageNodePort + 1, "").start();

    }

    public static void getLocalInfo() throws IOException {
        //FileServer启动后需要读取信息(结点信息和文件信息)
        if (!nodesLocal.exists()) {
            try {
                nodesLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        readLocalNodeset();
        if (!fileLocal.exists()) {
            try {
                fileLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        readLocalFileset();
        if (!userFileLocal.exists()) {
            try {
                userFileLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        readLocalUserFiles();
        if (!userNameLocal.exists()) {
            try {
                userNameLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        readLocalUsernames();
        if (!uuidNodeLocal.exists()) {
            try {
                uuidNodeLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        readLocalUuidNode();
    }

    public static void writeToLocalInfo() throws IOException {
        //Server关闭之后需要将信息序列化到文件中
        if (!nodesLocal.exists()) {
            try {
                nodesLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLocalNodeset();
        if (!fileLocal.exists()) {
            try {
                fileLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLocalFileset();
        if (!userFileLocal.exists()) {
            try {
                userFileLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLocalUserFiles();
        if (!userNameLocal.exists()) {
            try {
                userNameLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLocalUsernames();
        if (!uuidNodeLocal.exists()) {
            try {
                uuidNodeLocal.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeLocalUuisNode();
    }


    public static void readLocalFileset() throws IOException {
        FileInputStream fis = new FileInputStream(fileLocal);
        ObjectInputStream ois = new ObjectInputStream(fis);
        if (fileLocal.length() == 0) {
            return;
        }
        try {
            try {
                setFileSet((List<FileInfo>) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            ois.close();
            fis.close();
        }

    }

    public static void readLocalNodeset() throws IOException {
        FileInputStream fis = new FileInputStream(nodesLocal);
        ObjectInputStream ois = new ObjectInputStream(fis);
        if (nodesLocal.length() == 0) {
            return;
        }
        try {
            try {
                setNodeSet((ArrayList<NodeInfo>) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            ois.close();
            fis.close();
        }
    }

    public static void writeLocalFileset() throws IOException {
        FileOutputStream fos = new FileOutputStream(fileLocal);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(fileSet);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static void writeLocalNodeset() throws IOException {
        FileOutputStream fos = new FileOutputStream(nodesLocal);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(nodeSet);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static void readLocalUserFiles() throws IOException {
        FileInputStream fis = new FileInputStream(userFileLocal);
        ObjectInputStream ois = new ObjectInputStream(fis);
        if (userFileLocal.length() == 0) {
            return;
        }
        try {
            try {
                setUserFiles((HashMap<String, List<FileInfo>>) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            ois.close();
            fis.close();
        }
    }

    public static void writeLocalUserFiles() throws IOException {
        FileOutputStream fos = new FileOutputStream(userFileLocal);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(userFiles);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static void readLocalUsernames() throws IOException {
        FileInputStream fis = new FileInputStream(userNameLocal);
        ObjectInputStream ois = new ObjectInputStream(fis);
        if (userNameLocal.length() == 0) {
            return;
        }
        try {
            try {
                setUserNames((ArrayList<String>) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            ois.close();
            fis.close();
        }
    }

    public static void writeLocalUsernames() throws IOException {
        FileOutputStream fos = new FileOutputStream(userNameLocal);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(userNames);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static void readLocalUuidNode() throws IOException {
        FileInputStream fis = new FileInputStream(uuidNodeLocal);
        ObjectInputStream ois = new ObjectInputStream(fis);
        if (uuidNodeLocal.length() == 0) {
            return;
        }
        try {
            try {
                setUuidNode((Map<FileNum, NodeInfo>) ois.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            ois.close();
            fis.close();
        }
    }

    public static void writeLocalUuisNode() throws IOException {
        FileOutputStream fos = new FileOutputStream(uuidNodeLocal);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(uuidNode);
        oos.flush();
        oos.close();
        fos.close();
    }


    /**
     * @param fileLength 需要上传的文件大小
     * @return null    没有找到
     * freeNode    第一个满足大小的结点为主节点，剩余空间最大的结点为备份结点
     */
    public static NodeInfo[] findFreeNode(long fileLength) {
        for (NodeInfo info :
                getNodeSet()) {
            System.out.println(info.toString());
        }
        NodeInfo[] freeNode = new NodeInfo[2];
        Collections.sort(nodeSet, new Comparator<NodeInfo>() {
            @Override
            public int compare(NodeInfo o1, NodeInfo o2) {
                if (o1.getRemainCapacity() > o2.getRemainCapacity()) {
                    return 1;
                }
                if (o1.getRemainCapacity() < o2.getRemainCapacity()) {
                    return -1;
                }
                return 0;
            }
        });
        for (NodeInfo node :
                nodeSet) {
            System.out.println("结点信息：" + node.toString());
            if (node.getRemainCapacity() > fileLength) {
                freeNode[0] = node;
                System.out.println("最小满足的节点信息：" + freeNode[0].toString());
                break;
            }
        }
        System.out.println(nodeSet.get(nodeSet.size() - 1).toString());
        freeNode[1] = nodeSet.get(nodeSet.size() - 1);
//        freeNode[1] = freeNode[0];
        return freeNode;
    }


    public static class FileNum { //文件编号

        private UUID fileNum;

        public FileNum(UUID fileNum) {
            this.fileNum = fileNum;
        }

        public FileNum() {
            generateNum();
        }

        public UUID getFileNum() {
            return fileNum;
        }

        public void setFileNum(UUID fileNum) {
            this.fileNum = fileNum;
        }

        public void generateNum() {
            setFileNum(UUID.randomUUID());
        }

        @Override
        public String toString() {
            return fileNum.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileNum fileNum1 = (FileNum) o;

            return fileNum != null ? fileNum.equals(fileNum1.fileNum) : fileNum1.fileNum == null;
        }

        @Override
        public int hashCode() {
            return fileNum != null ? fileNum.hashCode() : 0;
        }

    }

    public static int getConfirmDownloadedPort() {
        return confirmDownloadedPort;
    }

    public static int getClientPort() {
        return clientPort;
    }

    public static int getStorageNodePort() {
        return storageNodePort;
    }

    public static List<String> getUserNames() {
        return userNames;
    }

    public static void setUserNames(List<String> userNames) {
        FileServer.userNames = userNames;
    }

    public ServerSocket getServeNodeSocket() {
        return serveNodeSocket;
    }

    public void setServeNodeSocket(ServerSocket serveNodeSocket) {
        this.serveNodeSocket = serveNodeSocket;
    }

    public ServerSocket getServeClientSocket() {
        return serveClientSocket;
    }


    public static List<FileInfo> getFileSet() {
        return fileSet;
    }

    public static void setFileSet(List<FileInfo> fileSet) {
        FileServer.fileSet = fileSet;
    }

    public static List<NodeInfo> getNodeSet() {
        return nodeSet;
    }

    public static void setNodeSet(List<NodeInfo> nodeSet) {
        FileServer.nodeSet = nodeSet;
    }

    public static Map<FileNum, NodeInfo> getUuidNode() {
        return uuidNode;
    }

    public static void setUuidNode(Map<FileNum, NodeInfo> uuidNode) {
        FileServer.uuidNode = uuidNode;
    }

    public static Map<String, List<FileInfo>> getUserFiles() {
        return userFiles;
    }

    public static void setUserFiles(Map<String, List<FileInfo>> userFiles) {
        FileServer.userFiles = userFiles;
    }

    public static int nodeExist(NodeInfo info) {
        for (NodeInfo temp :
                getNodeSet()) {
            if (temp.getName().equals(info.getName())) {
                return getNodeSet().indexOf(temp);
            }
        }
        return -1;
    }

    public static int getMonitorPort() {
        return monitorPort;
    }

    public static String getMonitorIp() {
        return monitorIp;
    }

}
