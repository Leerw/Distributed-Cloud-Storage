package Client;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by 巩汝何 on 2017/7/11.
 * 栗仁武 201592169
 */
public class FileClient {
    Socket s;
    String host;
    int port;
    DataInputStream dis;
    DataOutputStream dos;
    String UserName;
    //BufferedReader br;
    FileWriter fw;
    ZipComp zc;
    ZipDeCom zdc;
    FileClient()throws IOException//初始化
    {
        zc=new ZipComp();
        zdc=new ZipDeCom();
        //SetProperties();
        host="";
       // port=GetHostPort();//得到主机地址和端口然后建立tcp连接
        port=GetProperties();
        s=new Socket(host,port);
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        //br=new BufferedReader(dis);
    }
    int  GetHostPort() throws IOException {
        String ports="";
        FileReader setFile=new FileReader("ClientSet.txt");
        char c[]=new char[20];
        int length=setFile.read(c);
        for(int i=0;i<length;i++)
        {
            if(c[i]==' '){
                for(int j=0;j<i;j++)
                {
                    host+=c[j];
                }
                for(int j=i+1;j<length;j++)
                {
                    if(c[j]==' ')
                    {
                        for(int k=i+1;k<j;k++)
                            port+=c[k];
                        for(int k=j+1;k<length;k++)
                            UserName+=c[k];
                    }
                }
            }
        }
        return Integer.parseInt(ports);
    }
    int GetProperties() throws IOException {
        String port;
        Properties pps=new Properties();
        pps.load(new FileInputStream("ClientSet.properties")) ;
        host=pps.getProperty("ServerIp");
        port=pps.getProperty("ServerPort");
        UserName=pps.getProperty("Username");
        return Integer.parseInt(port);
    }
    int SetProperties() throws IOException {
        Properties pps=new Properties();
        InputStream in=new FileInputStream("ClientSet.properties");
        pps.load(in);
        pps.setProperty("ServerIp","123.456.789.345");//
        pps.setProperty("ServerPort","4321");
        pps.setProperty("Username","LiRenWu");
        //OutputStream out=new FileOutputStream("ClientSet.properties");
        return 0;
    }
    public static void main(String args[]) throws Exception {
        FileClient fc=new FileClient();
        /*switch (args[0])
        {
            case "upload"://在这里要调用文件上传程序
                fc.UpLoad(args[1]);
                break;
            case "remove":
                fc.remove(args[1]);
                break;
            case "download":
                fc.download(args[1],args[2]);
                break;
        }*/
        //fc.remove("bb7fa2e0-f526-43c6-a30c-a63d5db50ce5");
        fc.UpLoad("e:\\qianshi.mp4");
        //fc.download("2dcf3c68-d30d-409e-bf78-64cb414bd43d", "f:\\ass.mp4");
    }
    public int remove(String uuid) throws IOException {
        //传输命令到总服务器,移除文件命令依靠服务器向节点传递信息（此时服务器需要，通过UDP协议来实现）
        dos.writeInt(1);
        dos.writeChars(UserName+","+uuid+"#");

        dos.flush();
        char c=dis.readChar();//没有读到？
        if(c=='s')
            System.out.println("Remove "+uuid+" succeed!");
        else if(c=='f')
            System.out.println("Remove "+uuid+" failed!");
        return 0;
    }
    public int download(String uuid,String DestFile) throws Exception {
        //传输下载文件的命令到总服务器
        dos.writeInt(2);
        dos.writeChars(UserName+","+uuid+"#");
        dos.flush();
        String sb="";
        String ip1="",port1="",ip2="",port2="";
        char c;//服务器传输过来的数据格式"xxx.xxx.xxx.xxx 1234 xxx.xxx.xxx.xxx 1235q"
        System.out.println("overL:"+sb);
        while((c=dis.readChar())!='q')
        {
            System.out.print(c);
            sb+=c;//获得主、次节点的端口和ip地址
        }
        System.out.println("overL:"+sb);
        String[] tempStr = sb.split(" ");
        ip1 = tempStr[0];
        port1 = tempStr[1];
        ip2 = tempStr[2];
        port2 = tempStr[3];
        s.close();//关闭与服务器端的连接
        CreateLinkToNode(ip1,port1);//建立与主节点的链接
        //向主节点发送命令以及消息
        dos.writeInt(2);
        dos.writeChars(ip2+" "+port2+" "+UserName+" "+uuid+" q");
        dos.flush();

        c=dis.readChar();
        if(c=='s')
        {
            System.out.println("该节点存在该文件");
            byte[] inputByte = null;
            int length = 0;
            File f2=new File(uuid);
            if(!f2.exists())
                f2.createNewFile();
            FileOutputStream fout=new FileOutputStream(f2);//接收的文件放到uuid里面
            inputByte=new byte[1024];
            System.out.println("开始接受数据");
            while(true)
            {
                if(dis!=null)
                {
                    length = dis.read(inputByte, 0, inputByte.length);
                }
                if (length == -1) {
                    break;
                }
                System.out.println(length);
                fout.write(inputByte, 0, length);
                fout.flush();
            }
            System.out.println("完成接受数据");
            fout.close();
            dis.close();
            s.close();
            //开始进行解码
            FileEncryptAndDecrypt.decrypt(uuid,DestFile,5);
        }
        else
        {
            System.out.println("主节点，下载失败，连接备份节点");
            CreateLinkToNode(ip2,port2);
            dos.writeInt(2);
            dos.writeChars(ip2+" "+port2+" "+UserName+" "+uuid+"q");
            dos.flush();
            if(c=='s')
            {
                System.out.println("该节点存在该文件");
                byte[] inputByte = null;
                int length = 0;
                FileOutputStream fout=new FileOutputStream(uuid);//接收的文件放到uuid里面
                inputByte=new byte[1024];
                System.out.println("开始接受数据");
                while(true)
                {
                    if(dis!=null)
                    {
                        length = dis.read(inputByte, 0, inputByte.length);
                    }
                    if (length == -1) {
                        break;
                    }
                    System.out.println(length);
                    fout.write(inputByte, 0, length);
                    fout.flush();
                }
                System.out.println("完成接受数据");
                fout.close();
                dis.close();
                s.close();
                //开始进行解码


                zdc.DeZip(uuid,uuid+".nc");
                FileEncryptAndDecrypt.decrypt(uuid+".nc",DestFile,5);
                File fc=new File(uuid+".nc");
                if(fc.exists())
                    fc.delete();

            }
            else
                System.out.println("不存在此文件！下载失败！");
        }
        return 0;
    }
    public int UpLoad(String filepath) throws Exception {
        System.out.println("文件名字是：" + filepath);
        File f=new File(filepath);
        String SendMessage="";
        //传输信息到文件服务器
        dos.writeInt(0);
        SendMessage=""+UserName+","+f.getName()+","+f.length()+"#";
        System.out.println("想服务器发送请求： " + SendMessage);
        dos.writeChars(SendMessage);
        dos.flush();//强制输出缓存当中的数据
        //adsf
        //开始
        String sb="";
        String ip1="",port1="",ip2="",port2="",uuid="";
        char c;//服务器传输过来的数据格式"xxx.xxx.xxx.xxx 1234 xxx.xxx.xxx.xxx 1235 xxxxxxxq（uuid）"
        while((c=dis.readChar())!='q')
        {
            sb+=c;//获得主、次节点的端口和ip地址
        }
        System.out.println("服务器返回的信息是：" + sb);
        String temp[]=sb.split(" ");
        ip1=temp[0];
        port1=temp[1];
        ip2=temp[2];
        port2=temp[3];
        uuid = temp[4];

        s.close();//关闭与服务器端的连接
        CreateLinkToNode(ip1,port1);//建立与主节点的链接
        //建立完与主节点的连接之后要向主节点发送命令以及备份节点的ip,端口号,uuid
        dos.writeInt(0);

        dos.writeChars(ip2+","+port2+","+UserName+","+uuid+",q");

        dos.writeLong(f.length());//传文件大小过去
        dos.flush();
        if(dis.readChar()=='o')//可以传文件过去
        {//进行文件加密
            dos.writeChar('s');//开始传输文件
            dos.flush();
            System.out.println("uuid:"+uuid);
            //FileEncryptAndDecrypt.encrypt(filepath,"12345",uuid);//新建了一个加密过后的文件

            //压缩
            FileEncryptAndDecrypt.encrypt(filepath,"12345",uuid+".nz");//新建了一个加密过后的文件
            //加密完成之后应当进行压缩：
            File fz=new File(uuid+".nz");
            zc.zip(uuid,fz);
            if(fz.exists())
                fz.delete();//删掉文件副本


            String path = f.getPath();
            int index = path.lastIndexOf("\\");
            //String destFile = path.substring(0, index)+"\\"+uuid;//目标文件生成
            String destFile=uuid;
            File fsend =new File(destFile);
            if(f!=null)//如果文件存在
            {
                int length;
                FileInputStream fin=new FileInputStream(fsend);//传输文件
                byte[] sendByte=new byte[1024];
                //dos.writeUTF(uuid);//传输文件名过去？
                while((length = fin.read(sendByte, 0, sendByte.length))>0){
                    dos.write(sendByte,0,length);
                    dos.flush();
                }
                //然后关闭文件流和socket
                fin.close();
                dos.close();
                s.close();

                fsend.delete();//删除加密后的文件

                File f1=new File("HasUpload.txt");
                if(!f1.exists())
                    f1.createNewFile();

                FileWriter fw=new FileWriter(f1,true);

                fw.write("已经上传成功的文件："+filepath+" uuid:"+uuid+System.lineSeparator());
                fw.close();
            }
        }
        else
            System.out.println("传输失败");
        return 0;
    }
    public void CreateLinkToNode(String ip,String ports) throws IOException {
        InetAddress address=Inet4Address.getByName(ip) ;
        s = new Socket(address,Integer.parseInt(ports));
        dos=new DataOutputStream(s.getOutputStream());
        dis=new DataInputStream(s.getInputStream());

        //s=new Socket()t
    }
}
