package Server;

import Server.FileServer;
import Server.NodeStrategy;
import Server.ServeNodesThread;
import StorageNode.StorageNode;
import Util.FileInfo;
import Util.NodeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/13.
 */
public class ClientStrategy {

    private Socket client = null;

    public ClientStrategy(Socket client) {
        this.client = client;
    }

    public void service() throws IOException, InterruptedException {
        DataInputStream dis = new DataInputStream(client.getInputStream());
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        int command = dis.readInt();
        System.out.println(command);
        switch (command) {
            case 0://�ϴ�
                String message = "";
                char c;
                while ((c = dis.readChar()) != '#') {
                    message += c;
                }
                String[] temp = message.split(",");
                String userName = temp[0];
                String fileName = temp[1];
                String fileLength = temp[2];
                System.out.println("�û� " + userName + "Ҫ�ϴ� " + fileName);
                System.out.println("���ļ��Ĵ�СΪ��" + Long.parseLong(fileLength));
                for (NodeInfo info :
                        FileServer.getNodeSet()) {
                    System.out.println(info.toString());
                }

                NodeInfo node[] = FileServer.findFreeNode(Long.parseLong(fileLength));
                FileServer.FileNum num = new FileServer.FileNum();
                dos.writeChars(node[0].getIp() + " " + node[0].getPort() + " "
                        + node[1].getIp() + " " + node[1].getPort() + " " + num + 'q');

                FileInfo info = new FileInfo(userName, fileName, Long.parseLong(fileLength), num.toString(), node[0],
                        node[1]);

                System.out.println(info.toString());

                /**
                 * ȷ��confirm
                 */

//                ServeNodesThread confirmThread = new ServeNodesThread(new NodeStrategy(FileServer
//                        .getConfirmDownloadedPort(), 'c' + info.toString()));
                ServeNode confirmThread = new ServeNode(FileServer.getConfirmDownloadedPort(), 'c' + info.toString());
                confirmThread.start();
                Thread.sleep(2000);
                String confirmResult = confirmThread.getResult();
                System.out.println("confirmResult : " + confirmResult);
                if (confirmResult.equals("Null")) {
                    //�ϴ�ʧ�ܣ�
                    System.out.println("�Բ����ϴ�ʧ�ܣ������ԣ�");
                    break;
                } else {

                    //����֮ǰӦ��ȷ��һ��node��û�б�������ļ���Ҳ������û���ϴ��ɹ�
                    if (FileServer.getUserNames().contains(userName)) {
                        //�û��б�����������û���Ҳ����˵����û������û��Ļ����������ļ�Ŀ¼�м���һ���ļ�
                        if (!FileServer.getUserFiles().get(userName).contains(info)) {
                            FileServer.getUserFiles().get(userName).add(info);
                        } else {
                            System.out.println("���ļ����������ļ����д����ˣ�");
                        }
                    } else {
                        //���û�
                        FileServer.getUserNames().add(userName);    //���ּ����û�������
                        ArrayList<FileInfo> thisUserFiles = new ArrayList<>();
                        thisUserFiles.add(info);
                        System.out.println(thisUserFiles.size());
                        FileServer.getUserFiles().put(userName, thisUserFiles); //��������û����ļ�Ŀ¼
                    }

                    //


//                    if (!FileServer.getUserNames().contains(userName)) {
//                        FileServer.getUserNames().add(userName);
//                    }
//                    ArrayList<FileInfo> thisUserFiles = new ArrayList<>();
//                    thisUserFiles.add(info);
//                    FileServer.getUserFiles().put(userName, thisUserFiles);
////                    FileServer.getUserFiles().get(userName).add(info);
//                    FileServer.getFileSet().add(info);

                }
                System.out.println("���" + userName + "���ļ�����");
                for (FileInfo fileInfo :
                        FileServer.getUserFiles().get(userName)) {
                    System.out.println(fileInfo.toString());
                }
                break;
            case 1://ɾ��
                String recv = "";
                char ch;
                while ((ch = dis.readChar()) != '#') {
                    recv += ch;
                }
                String[] all = recv.split(",");
                String user = all[0];
                String removeFileID = all[1];
                System.out.println("Username " + user);
                System.out.println("removeFileID " + removeFileID);
                if (!FileServer.getUserNames().contains(user)) {
                    System.out.println("û������û����ļ���");
                    dos.writeChar('f');
                    break;
                }
                List<FileInfo> thisUserFiles = FileServer.getUserFiles().get(user);
                boolean found = false;
                FileInfo delFile = null;
                for (FileInfo infoTemp :
                        thisUserFiles) {
                    if (infoTemp.getUuidNum().equals(removeFileID)) {
                        found = true;
                        delFile = infoTemp;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("������û���������û�֮ǰ��û���ϴ�����ļ���");
                    dos.writeChar('f');
                    break;
                }
                String delMainIp = delFile.getMainNodeInfo().getIp();
                System.out.println("MainIp: " + delMainIp);
                String delMinorIp = delFile.getMinorNodeInfo().getIp();
                System.out.println("MinorIp: " + delMinorIp);
                int delMainPort = delFile.getMainNodeInfo().getPort() + 100;
                System.out.println("MainPort: " + delMainPort);
                int delMinorPort = delFile.getMinorNodeInfo().getPort() + 100;
                System.out.println("MinorPort: " + delMinorPort);
                //r����remove
                ServeNode mainThread = new ServeNode(FileServer.getStorageNodePort(),
                        "r" + delFile.getUserName() + " " + delFile.getUuidNum() + " " + "1" +
                                "," + delMainIp + "," + delMainPort);

                ServeNode minorThread = new ServeNode(FileServer.getStorageNodePort() - 1,
                        "r" + delFile.getUserName() + " " + delFile.getUuidNum() + " " + "0" +
                                "," + delMinorIp + "," + delMinorPort);
                System.out.println("FileServer.getStorageNodePort(): " + FileServer.getStorageNodePort());

                mainThread.start();
                minorThread.start();

//                Thread.sleep(1000);
                String mainResult;
                String minorResult;
                long time1 = System.currentTimeMillis();

                while (true) {
                    mainResult = mainThread.getResult();
                    minorResult = minorThread.getResult();
                    System.out.println("ClientStrategy.mainResult: " + mainResult);
                    System.out.println("ClientStrategy.minorResult: " + minorResult);
                    if (!mainResult.equals("Null") && !minorResult.equals("Null")) {
                        dos.writeChar('s');
                        System.out.println("ɾ���ɹ���");
                        break;
                    } else {
                        if (mainResult.equals("Null")) {
                            System.out.println("���ڵ�ɾ�����ļ�ʧ��");
                        }
                        if (minorResult.equals("Null")) {
                            System.out.println("���ݽ��ɾ�����ļ�ʧ��");
                        }
                    }
                    Thread.sleep(20);
                    if (System.currentTimeMillis() - time1 > 10000) {
                        System.out.println("��ʱ�������²�����");
                        dos.writeChar('f');
                        break;
                    }
                }
                break;
            case 2:
                //����
                String recvive = "";
                char cha;
                while ((cha = dis.readChar()) != '#') {
                    recvive += cha;
                }
                String[] allStr = recvive.split(",");
                String username = allStr[0];
                String downFileId = allStr[1];
                System.out.println("username " + username);
                System.out.println("downFileId " + downFileId);
                if (!FileServer.getUserNames().contains(username)) {
                    System.out.println("û������û����ļ���");
                    break;
                }
                List<FileInfo> thisUsersFiles = FileServer.getUserFiles().get(username);
                boolean isFound = false;
                FileInfo tmpFile = null;
                for (FileInfo infoTemp :
                        thisUsersFiles) {
                    if (infoTemp.getUuidNum().equals(downFileId)) {
                        isFound = true;
                        tmpFile = infoTemp;
                        break;
                    }
                }
                if (!isFound) {
                    System.out.println("������û���������û�֮ǰ��û���ϴ�����ļ���");
                    break;
                }

                String downMainIp = tmpFile.getMainNodeInfo().getIp();
                String downMinorIp = tmpFile.getMinorNodeInfo().getIp();

                System.out.println("mainIp: " + downMainIp);
                System.out.println("minorIp: " + downMinorIp);

                int mainNodePort = tmpFile.getMainNodeInfo().getPort();
                int minorNodePort = tmpFile.getMinorNodeInfo().getPort();

                //d����download
                //6003Ӧ�øĳɶ�Ӧ����port
//                new ServeNodesThread(new NodeStrategy(mainNodePort, "d" + tmpFile.toString() + "," + downMainIp))
//                        .start();
//                new ServeNodesThread(new NodeStrategy(minorNodePort, "d" + tmpFile.toString() + "," + downMinorIp))
//                        .start();
                System.out.println("d" + tmpFile.toString() + "," + downMainIp);
                System.out.println("d" + tmpFile.toString() + "," + downMinorIp);
                dos.writeChars(downMainIp + " " + mainNodePort + " " + downMinorIp + " " + minorNodePort + "q");
                System.out.println("\n\n" + downMainIp + " " + mainNodePort + " " + downMinorIp + " " + minorNodePort
                        + "q" + "\n\n");
                break;
        }
        dis.close();
        dos.flush();
        dos.close();

    }


}

