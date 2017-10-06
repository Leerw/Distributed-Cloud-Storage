package StorageNode;

import Util.NodeInfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 巩汝何 on 2017/7/13.
 */
public class SNodeServerThread implements Runnable{
    //Socket socket=null;//这个东西需要吗？？？好像不需要……？
    NodeInfo nodeInfo;
    int port;//端口和socket
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
    public void run()//在这里实现对文件的接收，传输，以及储存
    {
        try {
            ServerSocket serverSocket=new ServerSocket(port);//创建server
            Socket socket=null;
            System.out.println("服务器线程启动");
            while(true)//开始循环侦听
            {
                socket=serverSocket.accept();
                Thread thread=new Thread(new SNodeFileServer(socket,nodeInfo,ServerAddress,serverport,properties));
                thread.start();//线程启动
                System.out.println("文件服务线程启动");
                //以下是输出客户端的ip
                InetAddress address = socket.getInetAddress();
                System.out.println("当前客户端的IP为："+address.getHostAddress());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
