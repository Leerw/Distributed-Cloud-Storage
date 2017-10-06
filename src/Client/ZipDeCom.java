package Client;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by ����� on 2017/7/19.
 */
public class ZipDeCom {
        public static void DeZip(String filesource,String destfile) {
            // TODO Auto-generated method stub
            long startTime=System.currentTimeMillis();
            try {
                ZipInputStream Zin=new ZipInputStream(new FileInputStream(
                        filesource));//����Դzip·��
                BufferedInputStream Bin=new BufferedInputStream(Zin);
                String Parent=destfile; //���·�����ļ���Ŀ¼��
                File Fout=null;
                ZipEntry entry;
                try {
                    while((entry = Zin.getNextEntry())!=null && !entry.isDirectory()){
                        Fout=new File(Parent,entry.getName());
                        if(!Fout.exists()){
                            (new File(Fout.getParent())).mkdirs();
                        }
                        FileOutputStream out=new FileOutputStream(Fout);
                        BufferedOutputStream Bout=new BufferedOutputStream(out);
                        int b;
                        while((b=Bin.read())!=-1){
                            Bout.write(b);
                        }
                        Bout.close();
                        out.close();
                        System.out.println(Fout+"��ѹ�ɹ�");
                    }
                    Bin.close();
                    Zin.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            long endTime=System.currentTimeMillis();
            System.out.println("�ķ�ʱ�䣺 "+(endTime-startTime)+" ms");
        }
}
