package Util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/12.
 */
public class NodeInfo implements Comparable<NodeInfo>, Serializable {
    private String name;
    private String ip;
    private int port;
    private long capacity;
    private long actualCapacity;
    private long remainCapacity;
    private int fileNum;    //该结点所包含的文件数目
    private boolean canUse;
    private List<UUID> fileList = new ArrayList<>();

    public NodeInfo(String name, String ip, int port, long capacity, long actualCapacity, long remainCapacity, int fileNum, boolean canUse) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.capacity = capacity;
        this.actualCapacity = actualCapacity;
        this.remainCapacity = remainCapacity;
        this.fileNum = fileNum;
        this.canUse = canUse;
    }

    public NodeInfo(String info) {
        String[] allInfo = info.split(",");
        setName(allInfo[0]);
        setIp(allInfo[1]);
        setPort(Integer.parseInt(allInfo[2]));
        setCapacity(Long.parseLong(allInfo[3]));
        setActualCapacity(Long.parseLong(allInfo[4]));
        setRemainCapacity(Long.parseLong(allInfo[5]));
        setFileNum(Integer.parseInt(allInfo[6]));
        setCanUse(Boolean.parseBoolean(allInfo[7]));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getActualCapacity() {
        return actualCapacity;
    }

    public void setActualCapacity(long actualCapacity) {
        this.actualCapacity = actualCapacity;
    }

    public long getRemainCapacity() {
        return remainCapacity;
    }

    public void setRemainCapacity(long remainCapacity) {
        this.remainCapacity = remainCapacity;
    }

    public int getFileNum() {
        return fileNum;
    }

    public void setFileNum(int fileNum) {
        this.fileNum = fileNum;
    }

    public boolean isCanUse() {
        return canUse;
    }

    public void setCanUse(boolean canUse) {
        this.canUse = canUse;
    }

    public boolean hasFile(String Username, String uuid) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        if (port != nodeInfo.port) return false;
        if (capacity != nodeInfo.capacity) return false;
        if (actualCapacity != nodeInfo.actualCapacity) return false;
        if (remainCapacity != nodeInfo.remainCapacity) return false;
        if (fileNum != nodeInfo.fileNum) return false;
        if (canUse != nodeInfo.canUse) return false;
        if (name != null ? !name.equals(nodeInfo.name) : nodeInfo.name != null) return false;
        return ip != null ? ip.equals(nodeInfo.ip) : nodeInfo.ip == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (int) (capacity ^ (capacity >>> 32));
        result = 31 * result + (int) (actualCapacity ^ (actualCapacity >>> 32));
        result = 31 * result + (int) (remainCapacity ^ (remainCapacity >>> 32));
        result = 31 * result + fileNum;
        result = 31 * result + (canUse ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return name + "," + ip + "," + port + "," + capacity + ","  +
                actualCapacity + "," + remainCapacity + "," +
                fileNum + "," + canUse;
    }


    @Override
    public int compareTo(@NotNull NodeInfo o) {
        if (this.remainCapacity > o.remainCapacity) {
            return 1;
        }
        if (this.remainCapacity < o.remainCapacity) {
            return -1;
        }
        return 0;
    }
}
