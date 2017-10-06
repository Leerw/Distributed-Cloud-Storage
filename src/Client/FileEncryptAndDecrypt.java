package Client;
/**
 * Created by 巩汝何 on 2017/7/12.
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
     * 文件file进行加密
     *
     * @param fileUrl 文件路径
     * @param key     密码
     * @throws Exception
     */
    public static void encrypt(String fileUrl, String key, String DestFile) throws Exception {
        File file = new File(fileUrl);//得到文件的url
        String path = file.getPath();
        System.out.println("a");
        if (!file.exists()) {
            return;
        }
        int index = path.lastIndexOf("\\");
        System.out.println("index:"+index+" "+path.charAt(index) );
        //String destFile = path.substring(0, index)+"\\"+file.getName()+"-encrypted";//目标文件生成

        //String destFile = path.substring(0, index) + "\\" + DestFile;//目标文件生成

        File dest = new File(DestFile);
        InputStream in = new FileInputStream(fileUrl);
        OutputStream out = new FileOutputStream(dest);//在这里修改了
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
        // file.delete();//因为我们并不想删除文件所以就去掉这句话
        //dest.renameTo(new File(fileUrl));
        //appendMethodA(fileUrl, key);
        appendMethodA(DestFile, key);//这里就生成到一个新的文件当中就行了,ye gaile
        System.out.println("加密成功");
    }

    /**
     * @param fileName
     * @param content  密钥
     */
    public static void appendMethodA(String fileName, String content) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解密
     *
     * @param fileUrl 源文件
     * @param tempUrl 临时文件
     *                //* @param ketLength 密码长度
     * @return
     * @throws Exception
     */
    public static String decrypt(String fileUrl, String tempUrl, int keyLength) throws Exception {
        File file = new File(fileUrl);//这里输入的肯定是加密后的文件
        if (!file.exists()) {

            return null;
        }
        File dest = new File(tempUrl);
            /*if (!dest.getParentFile().exists()) {//这个是用来创建新的文件夹的
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
        System.out.println("解密成功？");
        return tempUrl;
    }

    /**
     * 判断文件是否加密
     *
     * @param fileName
     * @return
     */
    public static String readFileLastByte(String fileName, int keyLength) {
        File file = new File(fileName);
        if (!file.exists()) return null;
        StringBuffer str = new StringBuffer();
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "r");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
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
