package StorageNode;

import Util.NodeInfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ����� on 2017/7/13.
 */
public class SNodeServerThread implements Runnable{
    //Socket socket=null;//���������Ҫ�𣿣���������Ҫ������
    NodeInfo nodeInfo;
    int port;//�˿ں�socket
    Inet4Address ServerAddress;
    int serverport;
    String properties;
    public SNodeServerThread(int port,NodeInfo ni,Inet4Address sip,int sport,String properties)
    {
        this.port=port;
        nodeInfo=ni;
        //this.socket=socket;
        serverport=sport;
        ServerAddress=sip;
        this.properties=properties;
    }
    public void run()//������ʵ�ֶ��ļ��Ľ��գ����䣬�Լ�����
    {
        try {
            ServerSocket serverSocket=new ServerSocket(port);//����server
            Socket socket=null;
            System.out.println("�������߳�����");
            while(true)//��ʼѭ������
            {
                socket=serverSocket.accept();
                Thread thread=new Thread(new SNodeFileServer(socket,nodeInfo,ServerAddress,serverport,properties));
                thread.start();//�߳�����
                System.out.println("�ļ������߳�����");
                //����������ͻ��˵�ip
                InetAddress address = socket.getInetAddress();
                System.out.println("��ǰ�ͻ��˵�IPΪ��"+address.getHostAddress());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
