package Server;

import Util.FileInfo;
import Util.NodeInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/13.
 */
public class NodeStrategy {

    public DatagramSocket getServer() {
        return server;
    }

    private DatagramSocket server = null;
    private int port;
    private String command = "0";   //默认命令为"0"即接收Node发过来的注册信息
    /*
     * command = "0"; 什么都不干
     * command = uuid; 请求node删除uuid这个file
     */
/*
    public NodeStrategy(int port, String command) {
        this.port = port;
        if (!command.equals(""))
            this.command = command;
    }
    */

    public NodeStrategy(DatagramSocket server, String command) {
        this.server = server;
        if (!command.equals("")) {
            this.command = command;
        }
    }

    public String service() throws IOException {
        String result = "Null";
        switch (command.charAt(0)) {
            case '0'://什么都不干，就接受node的信息
                System.out.println("侦听来自node的UDP确认在线信息");
//                server = new DatagramSocket(port);//7001
//                DatagramSocket newServer = new DatagramSocket(7002);
                while (true) {
                    byte[] buf = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
                    server.receive(recvPacket);
                    String recvInfo = new String(recvPacket.getData(), 0, recvPacket.getLength());
                    System.out.println("NodeStrategy接收过来的信息是： " + recvInfo);
                    System.out.println("server port: " + server.getLocalPort());
                    NodeInfo nodeInfo = new NodeInfo(recvInfo);
                    System.out.println("节点信息为: " + nodeInfo.toString());
                    int index;
                    if (!((index = FileServer.nodeExist(nodeInfo)) == -1)) {
                        FileServer.getNodeSet().remove(index);
                    }
                    FileServer.getNodeSet().add(nodeInfo);

                    System.out.println("nodeSet:");
                    for (NodeInfo info :
                            FileServer.getNodeSet()) {
                        System.out.println(info.toString());
                    }

                    FileServer.isOnline.put(nodeInfo, true);    //接收到就置true，然后服务器每隔一段时间就检查一遍
                }
            case 'r'://删除
                System.out.println("NodeStrategy删除！");
                System.out.println("删除command: " + command);
                String[] message = command.substring(1, command.length()).split(",");
                String delFileStr = message[0];
                String delNodeIp = message[1];
                int delPort = Integer.parseInt(message[2]);
                String delMessage = "r " + delFileStr;
                System.out.println("删除文件信息： " + delMessage);
                System.out.println("delNodeIp: " + delNodeIp);
                //消息格式："r Username uuid"
                DatagramSocket sendSocket = new DatagramSocket();
                InetAddress delNodeAddr = InetAddress.getByName(delNodeIp);
//                System.out.println("DelPort: " + port);
                DatagramPacket delPacket = new DatagramPacket(delMessage.getBytes(), delMessage.getBytes().length, delNodeAddr,
                        delPort);
                System.out.println("DelPort: " + delPort);
                sendSocket.send(delPacket);
                sendSocket.close();
                //server.port = 7000
                byte[] confirmBuf = new byte[100];
                DatagramPacket confirmPacket = new DatagramPacket(confirmBuf, confirmBuf.length);
                server.receive(confirmPacket);
                String confirmStr = new String(confirmPacket.getData(), 0, confirmPacket.getLength());
                System.out.println("NodeStrategy.confirmStr: " + confirmStr);
                while (! confirmStr.equals("Delete Succeed")) {
                }
                result = "Delete Succeed";
                server.close();
                break;
            case 'c': //confirm
                System.out.println("确认文件是否被成功上传！");
//                server = new DatagramSocket(port);
                FileInfo confirmFile = new FileInfo(command.substring(1, command.length()));
                byte[] confirmBuffer = new byte[100];
                DatagramPacket confirmPck = new DatagramPacket(confirmBuffer, confirmBuffer.length);
                server.receive(confirmPck);
                String confirmMsg = new String(confirmPck.getData(), 0, confirmPck.getLength());
                System.out.println("confirmMsg: " + confirmMsg);
                if (confirmMsg.equals('u' + confirmFile.getUuidNum())) {
                    result = "Upload success";
                    System.out.println(result);
                }
                server.close();
                break;
        }
        return result;
    }
}


