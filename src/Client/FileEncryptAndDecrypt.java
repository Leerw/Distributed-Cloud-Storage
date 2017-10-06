package Client;
/**
 * Created by ����� on 2017/7/12.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class FileEncryptAndDecrypt {
    /**
     * �ļ�file���м���
     *
     * @param fileUrl �ļ�·��
     * @param key     ����
     * @throws Exception
     */
    public static void encrypt(String fileUrl, String key, String DestFile) throws Exception {
        File file = new File(fileUrl);//�õ��ļ���url
        String path = file.getPath();
        System.out.println("a");
        if (!file.exists()) {
            return;
        }
        int index = path.lastIndexOf("\\");
        System.out.println("index:"+index+" "+path.charAt(index) );
        //String destFile = path.substring(0, index)+"\\"+file.getName()+"-encrypted";//Ŀ���ļ�����

        //String destFile = path.substring(0, index) + "\\" + DestFile;//Ŀ���ļ�����

        File dest = new File(DestFile);
        InputStream in = new FileInputStream(fileUrl);
        OutputStream out = new FileOutputStream(dest);//�������޸���
        byte[] buffer = new byte[1024];
        int r;
        byte[] buffer2 = new byte[1024];
        while ((r = in.read(buffer)) > 0) {
            for (int i = 0; i < r; i++) {
                byte b = buffer[i];
                buffer2[i] = b == 255 ? 0 : ++b;
            }
            out.write(buffer2, 0, r);
            out.flush();
        }
        in.close();
        out.close();
        // file.delete();//��Ϊ���ǲ�����ɾ���ļ����Ծ�ȥ����仰
        //dest.renameTo(new File(fileUrl));
        //appendMethodA(fileUrl, key);
        appendMethodA(DestFile, key);//��������ɵ�һ���µ��ļ����о�����,ye gaile
        System.out.println("���ܳɹ�");
    }

    /**
     * @param fileName
     * @param content  ��Կ
     */
    public static void appendMethodA(String fileName, String content) {
        try {
            // ��һ����������ļ���������д��ʽ
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            // �ļ����ȣ��ֽ���
            long fileLength = randomFile.length();
            //��д�ļ�ָ���Ƶ��ļ�β��
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����
     *
     * @param fileUrl Դ�ļ�
     * @param tempUrl ��ʱ�ļ�
     *                //* @param ketLength ���볤��
     * @return
     * @throws Exception
     */
    public static String decrypt(String fileUrl, String tempUrl, int keyLength) throws Exception {
        File file = new File(fileUrl);//��������Ŀ϶��Ǽ��ܺ���ļ�
        if (!file.exists()) {

            return null;
        }
        File dest = new File(tempUrl);
            /*if (!dest.getParentFile().exists()) {//��������������µ��ļ��е�
                dest.getParentFile().mkdirs();
            }*/

        InputStream is = new FileInputStream(fileUrl);
        OutputStream out = new FileOutputStream(tempUrl);

        byte[] buffer = new byte[1024];
        byte[] buffer2 = new byte[1024];
        byte bMax = (byte) 255;
        long size = file.length() - keyLength;
        int mod = (int) (size % 1024);
        int div = (int) (size >> 10);
        int count = mod == 0 ? div : (div + 1);
        int k = 1, r;
        while ((k <= count && (r = is.read(buffer)) > 0)) {
            if (mod != 0 && k == count) {
                r = mod;
            }

            for (int i = 0; i < r; i++) {
                byte b = buffer[i];
                buffer2[i] = b == 0 ? bMax : --b;
            }
            out.write(buffer2, 0, r);
            k++;
        }
        out.close();
        is.close();
        System.out.println("���ܳɹ���");
        return tempUrl;
    }

    /**
     * �ж��ļ��Ƿ����
     *
     * @param fileName
     * @return
     */
    public static String readFileLastByte(String fileName, int keyLength) {
        File file = new File(fileName);
        if (!file.exists()) return null;
        StringBuffer str = new StringBuffer();
        try {
            // ��һ����������ļ���������д��ʽ
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "r");
            // �ļ����ȣ��ֽ���
            long fileLength = randomFile.length();
            //��д�ļ�ָ���Ƶ��ļ�β��
            for (int i = keyLength; i >= 1; i--) {
                randomFile.seek(fileLength - i);
                str.append((char) randomFile.read());
            }
            randomFile.close();
            return str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]) throws Exception {
       // encrypt("e:\\005.jpg","12345","aaaccc");
        decrypt("e:\\1dba2c57-d312-48ec-9d19-e187d6b6b07","e:\\test.jpg",5);
    }

}
