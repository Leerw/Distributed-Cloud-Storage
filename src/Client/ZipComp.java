package Client;

/**
 * Created by 巩汝何 on 2017/7/19.
 */
import java.io.*;
import java.util.zip.*;
public class ZipComp
{

        private int k = 1; // 定义递归次数变量

        public ZipComp() {
            // TODO Auto-generated constructor stub
        }
        public static void main(String[] args) {
            // TODO Auto-generated method stub
           ZipComp book = new ZipComp();
            try {
                book.zip("C:\\Users\\Gaowen\\Desktop\\ZipTestCompressing.zip",
                        new File("C:\\Users\\Gaowen\\Documents\\Tencent Files"));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        //前者是生成的文件，后者是需要压缩的文件
        public void zip(String zipFileName, File inputFile) throws Exception {
            System.out.println("压缩中...");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            BufferedOutputStream bo = new BufferedOutputStream(out);
            zip(out, inputFile, inputFile.getName(), bo);
            bo.close();
            out.close(); // 输出流关闭
            System.out.println("压缩完成");
        }

        private void zip(ZipOutputStream out, File f, String base,
                         BufferedOutputStream bo) throws Exception { // 方法重载
            if (f.isDirectory()) {
                File[] fl = f.listFiles();
                if (fl.length == 0) {
                    out.putNextEntry(new ZipEntry(base + "/")); // 创建zip压缩进入点base
                    System.out.println(base + "/");
                }
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + "/" + fl[i].getName(), bo); // 递归遍历子文件夹
                }
                System.out.println("第" + k + "次递归");
                k++;
            } else {
                out.putNextEntry(new ZipEntry(base)); // 创建zip压缩进入点base
                System.out.println(base);
                FileInputStream in = new FileInputStream(f);
                BufferedInputStream bi = new BufferedInputStream(in);
                int b;
                while ((b = bi.read()) != -1) {
                    bo.write(b); // 将字节流写入当前zip目录
                }
                bi.close();
                in.close(); // 输入流关闭
            }
        }


}
