package Util;

import java.io.Serializable;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/12.
 */
public class FileInfo implements Serializable{
    private String userName;

    private String uuidNum;

    private String fileName;
    private long fileLength;

    private NodeInfo mainNodeInfo;

    private NodeInfo minorNodeInfo;

    public FileInfo(String userName, String fileName, long fileLength, String uuidNum, NodeInfo mainNodeInfo,
                    NodeInfo minorNodeInfo) {
        this.userName = userName;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.uuidNum = uuidNum;
        this.mainNodeInfo = mainNodeInfo;
        this.minorNodeInfo = minorNodeInfo;
    }

    public FileInfo(String info) {

        String[] threeInfo = info.split(";");
        String fileSelf = threeInfo[0];

        String[] allInfo = fileSelf.split(",");
        this.userName = allInfo[0];
        this.fileName = allInfo[1];
        this.fileLength = Long.parseLong(allInfo[2]);
        this.uuidNum = allInfo[3];
        this.mainNodeInfo = new NodeInfo(threeInfo[1]);
        this.minorNodeInfo = new NodeInfo(threeInfo[2]);
    }

    @Override
    public String toString() {
        return this.userName + "," + this.fileName + "," + fileLength + "," + uuidNum + ";" + mainNodeInfo.toString()
                + ";" +
                minorNodeInfo
                        .toString();
    }



    public String getUuidNum() {
        return uuidNum;
    }



    public void setUuidNum(String uuidNum) {
        this.uuidNum = uuidNum;
    }


    public NodeInfo getMinorNodeInfo() {
        return minorNodeInfo;
    }

    public NodeInfo getMainNodeInfo() {
        return mainNodeInfo;
    }

    public void setMainNodeInfo(NodeInfo mainNodeInfo) {
        this.mainNodeInfo = mainNodeInfo;
    }

    public void setMinorNodeInfo(NodeInfo minorNodeInfo) {
        this.minorNodeInfo = minorNodeInfo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (fileLength != fileInfo.fileLength) return false;
        if (userName != null ? !userName.equals(fileInfo.userName) : fileInfo.userName != null) return false;
        if (uuidNum != null ? !uuidNum.equals(fileInfo.uuidNum) : fileInfo.uuidNum != null) return false;
        if (fileName != null ? !fileName.equals(fileInfo.fileName) : fileInfo.fileName != null) return false;
        if (mainNodeInfo != null ? !mainNodeInfo.equals(fileInfo.mainNodeInfo) : fileInfo.mainNodeInfo != null)
            return false;
        return minorNodeInfo != null ? minorNodeInfo.equals(fileInfo.minorNodeInfo) : fileInfo.minorNodeInfo == null;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (uuidNum != null ? uuidNum.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (int) (fileLength ^ (fileLength >>> 32));
        result = 31 * result + (mainNodeInfo != null ? mainNodeInfo.hashCode() : 0);
        result = 31 * result + (minorNodeInfo != null ? minorNodeInfo.hashCode() : 0);
        return result;
    }
}
