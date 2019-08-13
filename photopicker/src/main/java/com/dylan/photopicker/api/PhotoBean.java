package com.dylan.photopicker.api;

/**
 * Created by Dylan on 2017/5/23.
 */

public class PhotoBean {
    private boolean isSelected;
    private String path;

    public PhotoBean(String path){
          this.path=path;
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public String getPath() {
        if (path==null)path="";
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }


}
