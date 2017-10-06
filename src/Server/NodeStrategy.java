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
    private String command = "0";   //Ĭ������Ϊ"0"������Node��������ע����Ϣ
    /*
     * command = "0"; ʲô������
     * command = uuid; ����nodeɾ��uuid���file
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
            case '0'://ʲô�����ɣ��ͽ���node����Ϣ
                System.out.println("��������node��UDPȷ��������Ϣ");
//                server = new DatagramSocket(port);//7001
//                DatagramSocket newServer = new DatagramSocket(7002);
                while (true) {
                    byte[] buf = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
                    server.receive(recvPacket);
                    String recvInfo = new String(recvPacket.getData(), 0, recvPacket.getLength());
                    System.out.println("NodeStrategy���չ�������Ϣ�ǣ� " + recvInfo);
                    System.out.println("server port: " + server.getLocalPort());
                    NodeInfo nodeInfo = new NodeInfo(recvInfo);
                    System.out.println("�ڵ���ϢΪ: " + nodeInfo.toString());
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

                    FileServer.isOnline.put(nodeInfo, true);    //���յ�����true��Ȼ�������ÿ��һ��ʱ��ͼ��һ��
                }
            case 'r'://ɾ��
                System.out.println("NodeStrategyɾ����");
                System.out.println("ɾ��command: " + command);
                String[] message = command.substring(1, command.length()).split(",");
                String delFileStr = message[0];
                String delNodeIp = message[1];
                int delPort = Integer.parseInt(message[2]);
                String delMessage = "r " + delFileStr;
                System.out.println("ɾ���ļ���Ϣ�� " + delMessage);
                System.out.println("delNodeIp: " + delNodeIp);
                //��Ϣ��ʽ��"r Username uuid"
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
                System.out.println("ȷ���ļ��Ƿ񱻳ɹ��ϴ���");
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


