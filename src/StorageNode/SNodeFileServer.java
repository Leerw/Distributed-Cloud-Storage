package StorageNode;

import Util.NodeInfo;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by 巩汝何 on 2017/7/14.
 */
//实现文件的操作类，文件删除操作不通过这个tcp而是通过udp
public class SNodeFileServer implements Runnable {
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    NodeInfo nodeInfo;
    Inet4Address Sip;
    int Sport;
    String properties;

    SNodeFileServer(Socket socket, NodeInfo ni, Inet4Address serverip, int serverport, String properties) throws IOException {
        this.socket = socket;//建立连接以及 获得数据输出入输出流
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        nodeInfo = ni;
        Sport = serverport;
        Sip = serverip;
        this.properties = properties;
    }

    public void run() {
        try {
            int command = dis.readInt();//读取一个命令
            switch (command) {
                case 0://从客户端接收文件
                    FileAccept();
                    break;
                case 1:
                    BFileAccept();//从主节点来接收文件
                case 2:
                    FileSend();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //作为备份节点接收文件
    public void BFileAccept() throws IOException {
        //读取从主节点发过来的信息
        char c;
        String Msg = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        long filelength = 0;
        while ((c = dis.readChar()) != 'q')//格式为"Username uuid q"
        {
            Msg += c;
        }
        System.out.println("BFileAccept() Msg: " + Msg);
        String temp[] = Msg.split(" ");
        Username = temp[0];
        uuid = temp[1];
        System.out.println("uuid:" + uuid);
        filelength = dis.readLong();
        System.out.println("备份线程：USername" + Username + "uuid" + uuid + "剩余容量：" + nodeInfo.getRemainCapacity() + "filelength" + filelength);

        //判断容量是否足够存储

        if (nodeInfo.getRemainCapacity() > filelength) {
            System.out.println();
            dos.writeChar('o');
            dos.flush();
            if (dis.readChar() == 's')//开始传送文件
            { //执行文件接收程序
                System.out.println("bfjs");
                File dir = new File("f:\\" + nodeInfo.getName() + "\\" + Username);
                if (!dir.exists())
                    dir.mkdir();

                File F = new File("f:\\" + nodeInfo.getName() + "\\" + Username + "\\" + uuid);
                if (!F.exists())
                    F.createNewFile();

                FileOutputStream fos = new FileOutputStream(F);

                while (true) {
                    if (dis != null) {
                        length = dis.read(inputByte, 0, inputByte.length);
                    }
                    if (length == -1)
                        break;
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
                System.out.println("备份节点完成接收");
                //Confirm("Copy Succeed2");
                fos.close();
                dis.close();
                dos.close();
                socket.close();
            }
        } else {
            //dos.writeChar('n');
            System.out.println("节点容量不足");
        }
    }

    //这个函数是作为主节点来接收文件的程序
    public void FileAccept() throws IOException {
        //读取从客户端发过来的信息
        char c;
        String Msg = "";
        String BFip = "";
        String BFport = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        long filelength = 0;
        while ((c = dis.readChar()) != 'q')//格式为"xxx.xxx.xxx,1234,Username,uuid,q"
        {
            Msg += c;
        }
        System.out.println("Msg : " + Msg);
        String temp[] = Msg.split(",");
        BFip = temp[0];
        BFport = temp[1];
        Username = temp[2];
        uuid = temp[3];
        filelength = dis.readLong();
        System.out.println("ip:" + BFip + "asfd" + BFip.charAt(0) + "BP" + BFport + " UN" + Username + " UD" + uuid + " FL" + filelength);
        //判断容量是否足够存储01
        if (nodeInfo.getRemainCapacity() > filelength) {
            dos.writeChar('o');
            dos.flush();
            if (dis.readChar() == 's')//开始传送文件
            { //执行文件接收程序
                //System.out.println("UN:"+Username);
                System.out.println("ip:" + BFip + " " + BFport + " " + Username + " " + uuid + " " + filelength);
                File dir = new File("F:\\" + nodeInfo.getName() + "\\" + Username);
                if (!dir.exists())
                    dir.mkdir();

                File F = new File("F:\\" + nodeInfo.getName() + "\\" + Username + "\\" + uuid);
                if (!F.exists())
                    F.createNewFile();
                FileOutputStream fos = new FileOutputStream(F);

                while (true) {
                    if (dis != null) {
                        length = dis.read(inputByte, 0, inputByte.length);
                    }
                    if (length == -1)
                        break;
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
                System.out.println("完成接收");
                //Confirm("Receive Succeed");//像服务器发送消息确认

                Confirm('u' + uuid);
                if (F.exists()) {
                    nodeInfo.setRemainCapacity(nodeInfo.getRemainCapacity() - F.length());
                    // String message=String.valueOf();
                    System.out.println("rc" + nodeInfo.getRemainCapacity());
                    WriteTP("RemainCapacity", nodeInfo.getRemainCapacity() + "");
                    Confirmnode(nodeInfo.toString());
                }
                System.out.println("confirm信息：" + 'u' + uuid);
                fos.close();
                dis.close();
                dos.close();
                socket.close();

                LinkToBFNode(BFip, BFport);//接下来向备份节点传输文件请求
                System.out.println("备份结点的port:" + BFport + "已经链接成功了");
                System.out.println("info:" + socket.getRemoteSocketAddress() + " " + socket.getPort() + " ");
                dos.writeInt(1);
                dos.writeChars(Username + " " + uuid + " q");
                dos.writeLong(filelength);
                dos.flush();
                if (dis.readChar() == 'o') {
                    dos.writeChar('s');
                    dos.flush();
                    System.out.println("开始进行备份");
                    File F2 = new File("f:\\" + nodeInfo.getName() + "\\" + Username + "\\" + uuid);
                    File dir2 = new File("f:\\" + nodeInfo.getName() + "\\" + Username);
                    if (!dir2.exists()) {
                        dir2.mkdir();
                    }
                    if (!F2.exists()) {
                        F2.createNewFile();
                    }
                    FileInputStream fis = new FileInputStream(F2);
                    while ((length = fis.read(inputByte, 0, inputByte.length)) > 0) {
                        dos.write(inputByte, 0, length);
                        dos.flush();
                    }
                    System.out.println("备份完成");
//                    Confirm("Copy Succeed");

                    dis.close();
                    dos.close();
                    socket.close();
                } else {
                    System.out.println("备份失败！");
                }

            }
        } else {
//            dos.writeChar('n');
            System.out.println("节点容量不足");
        }
    }

    public void LinkToBFNode(String ip, String port) throws IOException {
        InetAddress address = Inet4Address.getByName(ip);
        socket = new Socket(address, Integer.parseInt(port));
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
    }

    public void FileSend() throws IOException {
        //读取从客户端发过来的信息
        char c;
        String Msg = "";
        String BFip = "";
        String BFport = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        while ((c = dis.readChar()) != 'q')//格式为"xxx.xxx.xxx 1234 Username uuidq"
        {
            Msg += c;
        }
        System.out.println("Message: " + Msg);

        String[] temp = Msg.split(" ");
        BFip = temp[0];
        BFport = temp[1];
        Username = temp[2];
        uuid = temp[3];

        if (nodeInfo.hasFile(Username, uuid))//主节点中存在这个文件
        {
            dos.writeChar('s');//直接发送s然后开始传输文件
            //dos.flush();
            if (true)//开始传送文件dis.readChar()=='s'
            { //执行文件发送程序
                File F2 = new File("f:\\" + nodeInfo.getName() + "\\" + Username + "\\" + uuid);
                FileInputStream fis = new FileInputStream(F2);
                while ((length = fis.read(inputByte, 0, inputByte.length)) > 0) {
                    dos.write(inputByte, 0, length);
                    //dos.flush();
                }
                System.out.println("文件传输成功");
//                Confirm("DownLoad succeed");
                dos.flush();

                dos.close();
                socket.close();
            }
        } else//主节点中不存在这个文件，向备份节点发送信息申请传过来
        {
            System.out.println("主节点中不存在这个文件需要从备份节点传输");
            dos.writeChar('n');
            dos.flush();
            dos.close();
            dis.close();
            socket.close();
        }
    }

    public void Confirm(String message) throws IOException {
        DatagramPacket DPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, Sip, 8000);
        DatagramSocket Dsocket = new DatagramSocket();
        Dsocket.send(DPacket);
    }

    public void Confirmnode(String message) throws IOException {
        DatagramPacket DPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, Sip, 7000);
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
