package Client;

/**
 * Created by ����� on 2017/7/19.
 */
import java.io.*;
import java.util.zip.*;
public class ZipComp
{

        private int k = 1; // ����ݹ��������

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

        //ǰ�������ɵ��ļ�����������Ҫѹ�����ļ�
        public void zip(String zipFileName, File inputFile) throws Exception {
            System.out.println("ѹ����...");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            BufferedOutputStream bo = new BufferedOutputStream(out);
            zip(out, inputFile, inputFile.getName(), bo);
            bo.close();
            out.close(); // ������ر�
            System.out.println("ѹ�����");
        }

        private void zip(ZipOutputStream out, File f, String base,
                         BufferedOutputStream bo) throws Exception { // ��������
            if (f.isDirectory()) {
                File[] fl = f.listFiles();
                if (fl.length == 0) {
                    out.putNextEntry(new ZipEntry(base + "/")); // ����zipѹ�������base
                    System.out.println(base + "/");
                }
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + "/" + fl[i].getName(), bo); // �ݹ�������ļ���
                }
                System.out.println("��" + k + "�εݹ�");
                k++;
            } else {
                out.putNextEntry(new ZipEntry(base)); // ����zipѹ�������base
                System.out.println(base);
                FileInputStream in = new FileInputStream(f);
                BufferedInputStream bi = new BufferedInputStream(in);
                int b;
                while ((b = bi.read()) != -1) {
                    bo.write(b); // ���ֽ���д�뵱ǰzipĿ¼
                }
                bi.close();
                in.close(); // �������ر�
            }
        }


}
