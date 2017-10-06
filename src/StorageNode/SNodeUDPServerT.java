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
 * Created by ����� on 2017/7/14.
 */
public class SNodeUDPServerT implements Runnable {
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    int port;//���port�����Ƿ�������port
    Inet4Address address;//��������ip��ַ
    NodeInfo nodeInfo;
    String properties;

    SNodeUDPServerT(int port, Inet4Address address, NodeInfo nodeInfo, String properties) throws SocketException {
        this.properties = properties;
//        socket = new DatagramSocket(port);//���port��ôѡ�������У����ѡ��
        this.port = port + 100;
        this.address = address;
        packet = new DatagramPacket(new byte[100], 0, 100);
        this.nodeInfo = nodeInfo;
    }

    @Override
    public void run() {//������������ļ���ɾ���Լ����
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
                //�����������������Ϣ
                //��Ϣ��ʽ��"r Username uuid 1/0",1Ϊ���ڵ㣬0Ϊ���ݽ��
                System.out.println("SNodeUDPSeverT.run.message: " + message);
                String[] temp = message.split(" ");
                String Command = temp[0];
                String Username = temp[1];
                String uuid = temp[2];
                String mode = temp[3];
                File fu = new File("F:" + File.separator +  nodeInfo.getName() + File.separator + Username + File
                        .separator +
                        uuid);
                //��û�б�Ҫ�����Ƿ����
                System.out.println("F:" + File.separator +  nodeInfo.getName() + File.separator + Username + File
                        .separator +
                        uuid);
                if (fu.exists())
                    System.out.println("�ļ�����");

                fu.delete();
                String delResult = "Delete Succeed";
                System.out.println("ɾ���ļ��ɹ���"+nodeInfo.getName());
                nodeInfo.setRemainCapacity(nodeInfo.getRemainCapacity() + fu.length());
                WriteTP("RemainCapacity", nodeInfo.getRemainCapacity() + "");//д���ļ�
                Confirmnode(nodeInfo.toString());
                DatagramSocket newSocket = new DatagramSocket();
                if (mode.equals("1")){
                    //׷���7000
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
