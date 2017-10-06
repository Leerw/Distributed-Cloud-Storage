package StorageNode;

import java.io.IOException;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/18.
 */
public class StorageNodeMain {
    public static void main(String args[]) throws IOException {
        StorageNode sn1=new StorageNode("NodeSet1.properties");
        StorageNode sn2=new StorageNode("NodeSet2.properties");
        StorageNode sn3=new StorageNode("NodeSet3.properties");
        StorageNode sn4=new StorageNode("NodeSet4.properties");
    }
}
