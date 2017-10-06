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
            case 0://上传
                String message = "";
                char c;
                while ((c = dis.readChar()) != '#') {
                    message += c;
                }
                String[] temp = message.split(",");
                String userName = temp[0];
                String fileName = temp[1];
                String fileLength = temp[2];
                System.out.println("用户 " + userName + "要上传 " + fileName);
                System.out.println("该文件的大小为：" + Long.parseLong(fileLength));
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
                 * 确认confirm
                 */

//                ServeNodesThread confirmThread = new ServeNodesThread(new NodeStrategy(FileServer
//                        .getConfirmDownloadedPort(), 'c' + info.toString()));
                ServeNode confirmThread = new ServeNode(FileServer.getConfirmDownloadedPort(), 'c' + info.toString());
                confirmThread.start();
                Thread.sleep(2000);
                String confirmResult = confirmThread.getResult();
                System.out.println("confirmResult : " + confirmResult);
                if (confirmResult.equals("Null")) {
                    //上传失败！
                    System.out.println("对不起！上传失败！请重试！");
                    break;
                } else {

                    //在这之前应该确认一下node有没有保存这个文件，也就是有没有上传成功
                    if (FileServer.getUserNames().contains(userName)) {
                        //用户列表里有了这个用户名也就是说这个用户是老用户的话就在他的文件目录中加上一个文件
                        if (!FileServer.getUserFiles().get(userName).contains(info)) {
                            FileServer.getUserFiles().get(userName).add(info);
                        } else {
                            System.out.println("该文件已在您的文件夹中存在了！");
                        }
                    } else {
                        //新用户
                        FileServer.getUserNames().add(userName);    //名字加入用户花名册
                        ArrayList<FileInfo> thisUserFiles = new ArrayList<>();
                        thisUserFiles.add(info);
                        System.out.println(thisUserFiles.size());
                        FileServer.getUserFiles().put(userName, thisUserFiles); //生成这个用户的文件目录
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
                System.out.println("输出" + userName + "的文件集合");
                for (FileInfo fileInfo :
                        FileServer.getUserFiles().get(userName)) {
                    System.out.println(fileInfo.toString());
                }
                break;
            case 1://删除
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
                    System.out.println("没有这个用户的文件！");
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
                    System.out.println("有这个用户但是这个用户之前并没有上传这个文件！");
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
                //r代表remove
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
                        System.out.println("删除成功！");
                        break;
                    } else {
                        if (mainResult.equals("Null")) {
                            System.out.println("主节点删除该文件失败");
                        }
                        if (minorResult.equals("Null")) {
                            System.out.println("备份结点删除该文件失败");
                        }
                    }
                    Thread.sleep(20);
                    if (System.currentTimeMillis() - time1 > 10000) {
                        System.out.println("超时！请重新操作！");
                        dos.writeChar('f');
                        break;
                    }
                }
                break;
            case 2:
                //下载
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
                    System.out.println("没有这个用户的文件！");
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
                    System.out.println("有这个用户但是这个用户之前并没有上传这个文件！");
                    break;
                }

                String downMainIp = tmpFile.getMainNodeInfo().getIp();
                String downMinorIp = tmpFile.getMinorNodeInfo().getIp();

                System.out.println("mainIp: " + downMainIp);
                System.out.println("minorIp: " + downMinorIp);

                int mainNodePort = tmpFile.getMainNodeInfo().getPort();
                int minorNodePort = tmpFile.getMinorNodeInfo().getPort();

                //d代表download
                //6003应该改成对应结点的port
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

