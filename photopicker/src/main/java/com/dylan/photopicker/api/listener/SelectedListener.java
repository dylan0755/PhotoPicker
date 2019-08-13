package com.dylan.photopicker.api.listener;


    /**
     * 图片选中和取消选中的监听
     */
    public interface SelectedListener {
        void addItem(String path, String maxSelection);

        void deleteItem(String path, String maxSelection);

        void selectedDir(String dirNmae);
}
