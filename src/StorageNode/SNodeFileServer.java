package StorageNode;

import Util.NodeInfo;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by ����� on 2017/7/14.
 */
//ʵ���ļ��Ĳ����࣬�ļ�ɾ��������ͨ�����tcp����ͨ��udp
public class SNodeFileServer implements Runnable {
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    NodeInfo nodeInfo;
    Inet4Address Sip;
    int Sport;
    String properties;

    SNodeFileServer(Socket socket, NodeInfo ni, Inet4Address serverip, int serverport, String properties) throws IOException {
        this.socket = socket;//���������Լ� �����������������
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        nodeInfo = ni;
        Sport = serverport;
        Sip = serverip;
        this.properties = properties;
    }

    public void run() {
        try {
            int command = dis.readInt();//��ȡһ������
            switch (command) {
                case 0://�ӿͻ��˽����ļ�
                    FileAccept();
                    break;
                case 1:
                    BFileAccept();//�����ڵ��������ļ�
                case 2:
                    FileSend();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //��Ϊ���ݽڵ�����ļ�
    public void BFileAccept() throws IOException {
        //��ȡ�����ڵ㷢��������Ϣ
        char c;
        String Msg = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        long filelength = 0;
        while ((c = dis.readChar()) != 'q')//��ʽΪ"Username uuid q"
        {
            Msg += c;
        }
        System.out.println("BFileAccept() Msg: " + Msg);
        String temp[] = Msg.split(" ");
        Username = temp[0];
        uuid = temp[1];
        System.out.println("uuid:" + uuid);
        filelength = dis.readLong();
        System.out.println("�����̣߳�USername" + Username + "uuid" + uuid + "ʣ��������" + nodeInfo.getRemainCapacity() + "filelength" + filelength);

        //�ж������Ƿ��㹻�洢

        if (nodeInfo.getRemainCapacity() > filelength) {
            System.out.println();
            dos.writeChar('o');
            dos.flush();
            if (dis.readChar() == 's')//��ʼ�����ļ�
            { //ִ���ļ����ճ���
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
                System.out.println("���ݽڵ���ɽ���");
                //Confirm("Copy Succeed2");
                fos.close();
                dis.close();
                dos.close();
                socket.close();
            }
        } else {
            //dos.writeChar('n');
            System.out.println("�ڵ���������");
        }
    }

    //�����������Ϊ���ڵ��������ļ��ĳ���
    public void FileAccept() throws IOException {
        //��ȡ�ӿͻ��˷���������Ϣ
        char c;
        String Msg = "";
        String BFip = "";
        String BFport = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        long filelength = 0;
        while ((c = dis.readChar()) != 'q')//��ʽΪ"xxx.xxx.xxx,1234,Username,uuid,q"
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
        //�ж������Ƿ��㹻�洢01
        if (nodeInfo.getRemainCapacity() > filelength) {
            dos.writeChar('o');
            dos.flush();
            if (dis.readChar() == 's')//��ʼ�����ļ�
            { //ִ���ļ����ճ���
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
                System.out.println("��ɽ���");
                //Confirm("Receive Succeed");//�������������Ϣȷ��

                Confirm('u' + uuid);
                if (F.exists()) {
                    nodeInfo.setRemainCapacity(nodeInfo.getRemainCapacity() - F.length());
                    // String message=String.valueOf();
                    System.out.println("rc" + nodeInfo.getRemainCapacity());
                    WriteTP("RemainCapacity", nodeInfo.getRemainCapacity() + "");
                    Confirmnode(nodeInfo.toString());
                }
                System.out.println("confirm��Ϣ��" + 'u' + uuid);
                fos.close();
                dis.close();
                dos.close();
                socket.close();

                LinkToBFNode(BFip, BFport);//�������򱸷ݽڵ㴫���ļ�����
                System.out.println("���ݽ���port:" + BFport + "�Ѿ����ӳɹ���");
                System.out.println("info:" + socket.getRemoteSocketAddress() + " " + socket.getPort() + " ");
                dos.writeInt(1);
                dos.writeChars(Username + " " + uuid + " q");
                dos.writeLong(filelength);
                dos.flush();
                if (dis.readChar() == 'o') {
                    dos.writeChar('s');
                    dos.flush();
                    System.out.println("��ʼ���б���");
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
                    System.out.println("�������");
//                    Confirm("Copy Succeed");

                    dis.close();
                    dos.close();
                    socket.close();
                } else {
                    System.out.println("����ʧ�ܣ�");
                }

            }
        } else {
//            dos.writeChar('n');
            System.out.println("�ڵ���������");
        }
    }

    public void LinkToBFNode(String ip, String port) throws IOException {
        InetAddress address = Inet4Address.getByName(ip);
        socket = new Socket(address, Integer.parseInt(port));
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
    }

    public void FileSend() throws IOException {
        //��ȡ�ӿͻ��˷���������Ϣ
        char c;
        String Msg = "";
        String BFip = "";
        String BFport = "";
        String uuid = "";
        String Username = "";
        byte[] inputByte = new byte[1024];
        int length = 0;
        while ((c = dis.readChar()) != 'q')//��ʽΪ"xxx.xxx.xxx 1234 Username uuidq"
        {
            Msg += c;
        }
        System.out.println("Message: " + Msg);

        String[] temp = Msg.split(" ");
        BFip = temp[0];
        BFport = temp[1];
        Username = temp[2];
        uuid = temp[3];

        if (nodeInfo.hasFile(Username, uuid))//���ڵ��д�������ļ�
        {
            dos.writeChar('s');//ֱ�ӷ���sȻ��ʼ�����ļ�
            //dos.flush();
            if (true)//��ʼ�����ļ�dis.readChar()=='s'
            { //ִ���ļ����ͳ���
                File F2 = new File("f:\\" + nodeInfo.getName() + "\\" + Username + "\\" + uuid);
                FileInputStream fis = new FileInputStream(F2);
                while ((length = fis.read(inputByte, 0, inputByte.length)) > 0) {
                    dos.write(inputByte, 0, length);
                    //dos.flush();
                }
                System.out.println("�ļ�����ɹ�");
//                Confirm("DownLoad succeed");
                dos.flush();

                dos.close();
                socket.close();
            }
        } else//���ڵ��в���������ļ����򱸷ݽڵ㷢����Ϣ���봫����
        {
            System.out.println("���ڵ��в���������ļ���Ҫ�ӱ��ݽڵ㴫��");
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
