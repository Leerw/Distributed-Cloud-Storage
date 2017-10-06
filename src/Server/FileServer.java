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
 * һ��FileServerӦ�ã���ʱ�����Ƕ��FileServerӦ�ã����ṩ�ļ��洢�ڵ�StorageNode�Ĺ����ܣ��ṩ�ļ��Ĺ����ܡ�
 * FileServerӦ���ṩ�ļ��洢�ڵ�Ĺ����ṩ�洢�ļ��Ĺ����ܡ�
 * FileClient�ڽ����ļ��ϴ�������ʱ������ͨ��FileServer����ȡ�ļ��洢���ĸ��ڵ㣬�ĸ����ݽڵ��һЩ�ļ���Ϣ��
 * FileServer����ÿ���ļ�����Ҫͨ���ļ��������ɣ������ڴ���ͨ��Map���������������ļ���
 * ÿ���ļ�����Ҫ�������ڵ��Ͻ���1+1���ݴ洢��FileServer����洢�ڵ���Ҫ���Ǹ��ؾ��⡣
 * �ϴ��ɹ���FileServer��Ҫ���ļ������Ϣ���ݸ��ͻ��ˣ��ͻ����Ժ�ͨ�����ļ���Ž����ļ����أ��ļ�ɾ����
 * ��FileServer���������ļ���Ϣ���ļ���Ų���UUID, UUID.randomUUID().toString()
 * ��FileServer����Ҫ�������д洢�ڵ���Ϣ��
 * FileServer���ڴ�����Ҫ������FileStorage����������Ϣ���������ƣ�ip���˿ڣ�
 * ������ʵ��������ʣ���������ļ��������Ƿ���õ���Ϣ��
 * �ļ���Ϣ��������ţ��ļ�ԭʼ���ƣ��ļ���С�����洢�ڵ���Ϣ�����ݽڵ���Ϣ���ȵȡ�
 * ��Щ��Ϣ����ʹ�ü��������д洢��ͬʱҪ��֧�����л����ļ��С���FileServer��������Ҫ��ȡ��Щ���л���Ϣ���������˳�ʱ����Ҫ
 * ������Щ���л���Ϣ���ļ��С�
 */
public class FileServer {

    private static final int clientPort = 6000;       //���������տͻ��˵Ķ˿�
    private static final int storageNodePort = 7000;  //�������������н���ע����Ϣ
    private static final int confirmDownloadedPort = 8000;
    private static final int monitorPort = 10000;
    private static final String monitorIp = "192.168.1.1";


    private ServerSocket serveClientSocket;
    private ServerSocket serveNodeSocket;
    public static Map<NodeInfo, Boolean> isOnline = new HashMap<>();       //�洢StorageNodes��������Ϣ
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
            System.out.println("�û��� " + userName);
        }
/*
        CommunicateMonitor startMonitor = new CommunicateMonitor(monitorIp, monitorPort);
        ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();
        monitorService.scheduleAtFixedRate(startMonitor,10,100,TimeUnit.MILLISECONDS);
        */

        //new ServeNodesThread(new NodeStrategy(storageNodePort,"0")).start();

        Runnable runnable = () -> {
            //�ж���û�н�����
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
        //����һ��Ĭ�ϵķ������̣߳�Ҳ���ǽ�������Node��ע����Ϣ
//        new ServeNodesThread(new NodeStrategy(storageNodePort,"")).start();
//        new ServeNodesThread(new NodeStrategy(storageNodePort + 1, "")).start();
        new ServeNode(storageNodePort + 1, "").start();

    }

    public static void getLocalInfo() throws IOException {
        //FileServer��������Ҫ��ȡ��Ϣ(�����Ϣ���ļ���Ϣ)
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
        //Server�ر�֮����Ҫ����Ϣ���л����ļ���
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
     * @param fileLength ��Ҫ�ϴ����ļ���С
     * @return null    û���ҵ�
     * freeNode    ��һ�������С�Ľ��Ϊ���ڵ㣬ʣ��ռ����Ľ��Ϊ���ݽ��
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
            System.out.println("�����Ϣ��" + node.toString());
            if (node.getRemainCapacity() > fileLength) {
                freeNode[0] = node;
                System.out.println("��С����Ľڵ���Ϣ��" + freeNode[0].toString());
                break;
            }
        }
        System.out.println(nodeSet.get(nodeSet.size() - 1).toString());
        freeNode[1] = nodeSet.get(nodeSet.size() - 1);
//        freeNode[1] = freeNode[0];
        return freeNode;
    }


    public static class FileNum { //�ļ����

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
