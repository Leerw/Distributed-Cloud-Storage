package StorageNode;


import Util.NodeInfo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by 巩汝何 on 2017/7/14.
 */
public class SNodeUDPServerT implements Runnable {
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    int port;//这个port必须是服务器的port
    Inet4Address address;//服务器的ip地址
    NodeInfo nodeInfo;
    String properties;

    SNodeUDPServerT(int port, Inet4Address address, NodeInfo nodeInfo, String properties) throws SocketException {
        this.properties = properties;
//        socket = new DatagramSocket(port);//这个port怎么选……都行？随便选？
        this.port = port + 100;
        this.address = address;
        packet = new DatagramPacket(new byte[100], 0, 100);
        this.nodeInfo = nodeInfo;
    }

    @Override
    public void run() {//由这个来处理文件的删除以及完成
        System.out.println("SNodeUDPServer.run");
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                System.out.println("SNodeUDPServerT.port: " + port);
                System.out.println("SNodeUDPServerT.socket.port: " + socket.getLocalPort());
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                //处理服务器发来的消息
                //消息格式："r Username uuid 1/0",1为主节点，0为备份结点
                System.out.println("SNodeUDPSeverT.run.message: " + message);
                String[] temp = message.split(" ");
                String Command = temp[0];
                String Username = temp[1];
                String uuid = temp[2];
                String mode = temp[3];
                File fu = new File("F:" + File.separator +  nodeInfo.getName() + File.separator + Username + File
                        .separator +
                        uuid);
                //有没有必要检验是否存在
                System.out.println("F:" + File.separator +  nodeInfo.getName() + File.separator + Username + File
                        .separator +
                        uuid);
                if (fu.exists())
                    System.out.println("文件存在");

                fu.delete();
                String delResult = "Delete Succeed";
                System.out.println("删除文件成功："+nodeInfo.getName());
                nodeInfo.setRemainCapacity(nodeInfo.getRemainCapacity() + fu.length());
                WriteTP("RemainCapacity", nodeInfo.getRemainCapacity() + "");//写入文件
                Confirmnode(nodeInfo.toString());
                DatagramSocket newSocket = new DatagramSocket();
                if (mode.equals("1")){
                    //追结点7000
                    packet = new DatagramPacket(delResult.getBytes(), delResult.getBytes().length, address, 7000);
                    newSocket.send(packet);
                }
                if (mode.equals("0")) {
                    packet = new DatagramPacket(delResult.getBytes(), delResult.getBytes().length, address, 6999);
                    newSocket.send(packet);
                }
                newSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Confirmnode(String message) throws IOException {
        DatagramPacket DPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, address, 7001);
        DatagramSocket Dsocket = new DatagramSocket();
        Dsocket.send(DPacket);
    }

    public void WriteTP(String key, String value) throws IOException {


        InputStream input = new FileInputStream(properties);
        Properties pps = new Properties();
        pps.load(input);
        OutputStream output = new FileOutputStream(properties);
        pps.setProperty(key, value);
        pps.store(output, "add" + new Date().toString());
    }
}
