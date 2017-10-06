package StorageNode;

/**
 * Created by ����� on 2017/7/14.
 */

import Util.NodeInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;


public class SNodeUDPSenderT implements Runnable {
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    NodeInfo ni;
    int port;
    Inet4Address address = null;
    String properties;
    SNodeUDPSenderT(int dport, Inet4Address daddress, NodeInfo ni,String properties) throws SocketException {


        this.properties=properties;
        socket = new DatagramSocket();
       // new DatagramSocket()

        System.out.println("port+"+dport+" "+daddress);
        port = dport;
        this.ni = ni;
        address = daddress;
    }

    @Override
    public void run() {
        String ACK = "";
//        packet = new DatagramPacket(ni.getName().getBytes(), ni.getName().length(),address,port);


        System.out.println("�����Ľ����Ϣ�� " + ni.toString());
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    try {
                        UpDate();//���½�����Ϣ
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    packet = new DatagramPacket(ni.toString().getBytes(), ni.toString().length(), address, port);
                    socket.send(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 2000);
    }
    public void UpDate() throws IOException {
        Properties pps=new Properties();
        pps.load(new FileInputStream(properties)) ;
        String rc=pps.getProperty("RemainCapacity");
        ni.setRemainCapacity(Long.parseLong(rc));
    }
}
