package com.dylan.photopicker.api;

/**
 * Created by AD on 2016/4/25.
 */
public class PhotoAlbum {
    private String name;
    private String dirPath;
    private String firstChildPath;
    private int  childCount;
    public PhotoAlbum(){
        name="";
        dirPath="";
        firstChildPath ="";
        childCount =0;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name=name;
    }
    public String getDirPath() {
        return dirPath;
    }
    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
    public String getFirstChildPath() {
        return firstChildPath;
    }
    public void setFirstChildPath(String firstChildPath) {
        this.firstChildPath = firstChildPath;
    }
    public int getChildCount() {
        return childCount;
    }
    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

}
