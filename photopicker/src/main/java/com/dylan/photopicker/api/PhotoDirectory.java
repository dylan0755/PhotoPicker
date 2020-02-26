package com.dylan.photopicker.api;

import com.dylan.library.media.PictureDirectory;

/**
 * Created by AD on 2016/4/25.
 */
public class PhotoDirectory {
    private String dirName;
    private String dirPath;
    private String firstChildPath;
    private int  childCount;
    public PhotoDirectory(){
        dirName ="";
        dirPath="";
        firstChildPath ="";
        childCount =0;
    }
    public String getDirName() {
        return dirName;
    }
    public void setDirName(String dirName) {
        this.dirName = dirName;
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



    public static PhotoDirectory dto(PictureDirectory directory){
        PhotoDirectory photoDirectory=new PhotoDirectory();
        photoDirectory.setChildCount(directory.getChildCount());
        photoDirectory.setDirName(directory.getDirName());
        photoDirectory.setDirPath(directory.getDirPath());
        photoDirectory.setFirstChildPath(directory.getFirstChildPath());
        return photoDirectory;
    }
}
