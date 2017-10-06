package Client;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by ����� on 2017/7/11.
 * ������ 201592169
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
    FileClient()throws IOException//��ʼ��
    {
        zc=new ZipComp();
        zdc=new ZipDeCom();
        //SetProperties();
        host="";
       // port=GetHostPort();//�õ�������ַ�Ͷ˿�Ȼ����tcp����
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
            case "upload"://������Ҫ�����ļ��ϴ�����
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
        //��������ܷ�����,�Ƴ��ļ�����������������ڵ㴫����Ϣ����ʱ��������Ҫ��ͨ��UDPЭ����ʵ�֣�
        dos.writeInt(1);
        dos.writeChars(UserName+","+uuid+"#");

        dos.flush();
        char c=dis.readChar();//û�ж�����
        if(c=='s')
            System.out.println("Remove "+uuid+" succeed!");
        else if(c=='f')
            System.out.println("Remove "+uuid+" failed!");
        return 0;
    }
    public int download(String uuid,String DestFile) throws Exception {
        //���������ļ�������ܷ�����
        dos.writeInt(2);
        dos.writeChars(UserName+","+uuid+"#");
        dos.flush();
        String sb="";
        String ip1="",port1="",ip2="",port2="";
        char c;//������������������ݸ�ʽ"xxx.xxx.xxx.xxx 1234 xxx.xxx.xxx.xxx 1235q"
        System.out.println("overL:"+sb);
        while((c=dis.readChar())!='q')
        {
            System.out.print(c);
            sb+=c;//��������νڵ�Ķ˿ں�ip��ַ
        }
        System.out.println("overL:"+sb);
        String[] tempStr = sb.split(" ");
        ip1 = tempStr[0];
        port1 = tempStr[1];
        ip2 = tempStr[2];
        port2 = tempStr[3];
        s.close();//�ر���������˵�����
        CreateLinkToNode(ip1,port1);//���������ڵ������
        //�����ڵ㷢�������Լ���Ϣ
        dos.writeInt(2);
        dos.writeChars(ip2+" "+port2+" "+UserName+" "+uuid+" q");
        dos.flush();

        c=dis.readChar();
        if(c=='s')
        {
            System.out.println("�ýڵ���ڸ��ļ�");
            byte[] inputByte = null;
            int length = 0;
            File f2=new File(uuid);
            if(!f2.exists())
                f2.createNewFile();
            FileOutputStream fout=new FileOutputStream(f2);//���յ��ļ��ŵ�uuid����
            inputByte=new byte[1024];
            System.out.println("��ʼ��������");
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
            System.out.println("��ɽ�������");
            fout.close();
            dis.close();
            s.close();
            //��ʼ���н���
            FileEncryptAndDecrypt.decrypt(uuid,DestFile,5);
        }
        else
        {
            System.out.println("���ڵ㣬����ʧ�ܣ����ӱ��ݽڵ�");
            CreateLinkToNode(ip2,port2);
            dos.writeInt(2);
            dos.writeChars(ip2+" "+port2+" "+UserName+" "+uuid+"q");
            dos.flush();
            if(c=='s')
            {
                System.out.println("�ýڵ���ڸ��ļ�");
                byte[] inputByte = null;
                int length = 0;
                FileOutputStream fout=new FileOutputStream(uuid);//���յ��ļ��ŵ�uuid����
                inputByte=new byte[1024];
                System.out.println("��ʼ��������");
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
                System.out.println("��ɽ�������");
                fout.close();
                dis.close();
                s.close();
                //��ʼ���н���


                zdc.DeZip(uuid,uuid+".nc");
                FileEncryptAndDecrypt.decrypt(uuid+".nc",DestFile,5);
                File fc=new File(uuid+".nc");
                if(fc.exists())
                    fc.delete();

            }
            else
                System.out.println("�����ڴ��ļ�������ʧ�ܣ�");
        }
        return 0;
    }
    public int UpLoad(String filepath) throws Exception {
        System.out.println("�ļ������ǣ�" + filepath);
        File f=new File(filepath);
        String SendMessage="";
        //������Ϣ���ļ�������
        dos.writeInt(0);
        SendMessage=""+UserName+","+f.getName()+","+f.length()+"#";
        System.out.println("��������������� " + SendMessage);
        dos.writeChars(SendMessage);
        dos.flush();//ǿ��������浱�е�����
        //adsf
        //��ʼ
        String sb="";
        String ip1="",port1="",ip2="",port2="",uuid="";
        char c;//������������������ݸ�ʽ"xxx.xxx.xxx.xxx 1234 xxx.xxx.xxx.xxx 1235 xxxxxxxq��uuid��"
        while((c=dis.readChar())!='q')
        {
            sb+=c;//��������νڵ�Ķ˿ں�ip��ַ
        }
        System.out.println("���������ص���Ϣ�ǣ�" + sb);
        String temp[]=sb.split(" ");
        ip1=temp[0];
        port1=temp[1];
        ip2=temp[2];
        port2=temp[3];
        uuid = temp[4];

        s.close();//�ر���������˵�����
        CreateLinkToNode(ip1,port1);//���������ڵ������
        //�����������ڵ������֮��Ҫ�����ڵ㷢�������Լ����ݽڵ��ip,�˿ں�,uuid
        dos.writeInt(0);

        dos.writeChars(ip2+","+port2+","+UserName+","+uuid+",q");

        dos.writeLong(f.length());//���ļ���С��ȥ
        dos.flush();
        if(dis.readChar()=='o')//���Դ��ļ���ȥ
        {//�����ļ�����
            dos.writeChar('s');//��ʼ�����ļ�
            dos.flush();
            System.out.println("uuid:"+uuid);
            //FileEncryptAndDecrypt.encrypt(filepath,"12345",uuid);//�½���һ�����ܹ�����ļ�

            //ѹ��
            FileEncryptAndDecrypt.encrypt(filepath,"12345",uuid+".nz");//�½���һ�����ܹ�����ļ�
            //�������֮��Ӧ������ѹ����
            File fz=new File(uuid+".nz");
            zc.zip(uuid,fz);
            if(fz.exists())
                fz.delete();//ɾ���ļ�����


            String path = f.getPath();
            int index = path.lastIndexOf("\\");
            //String destFile = path.substring(0, index)+"\\"+uuid;//Ŀ���ļ�����
            String destFile=uuid;
            File fsend =new File(destFile);
            if(f!=null)//����ļ�����
            {
                int length;
                FileInputStream fin=new FileInputStream(fsend);//�����ļ�
                byte[] sendByte=new byte[1024];
                //dos.writeUTF(uuid);//�����ļ�����ȥ��
                while((length = fin.read(sendByte, 0, sendByte.length))>0){
                    dos.write(sendByte,0,length);
                    dos.flush();
                }
                //Ȼ��ر��ļ�����socket
                fin.close();
                dos.close();
                s.close();

                fsend.delete();//ɾ�����ܺ���ļ�

                File f1=new File("HasUpload.txt");
                if(!f1.exists())
                    f1.createNewFile();

                FileWriter fw=new FileWriter(f1,true);

                fw.write("�Ѿ��ϴ��ɹ����ļ���"+filepath+" uuid:"+uuid+System.lineSeparator());
                fw.close();
            }
        }
        else
            System.out.println("����ʧ��");
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
