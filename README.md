
# 添加依赖  工程build 加上JCenter

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
     .....
    }
}

app modules 中

compile 'com.dylan:photopicker:1.0.0'


# 选择图片跳转

public void openPicker(View view) {
        Intent intent = new Intent(view.getContext(), PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_MAX_SELECT,3);
        intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA_MULTI_CHOICE,false);//多选的时候显示相机入口
        //intent.putExtra(PhotoPickerActivity.EXTRA_NEED_SHOW_SELECT_LIMIT,true);
       // intent.putExtra(PhotoPickerActivity.EXTRA_LIMIT_SELECT_TIP,"不可以再选了");
        startActivityForResult(intent, 1001);
    }

# 选择图片的路径数组

 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            if (data==null)return;
            String[] array = data.getStringArrayExtra(PhotoPickerActivity.IMAGE_SELECT_ARRAY);
            if (array!=null){
               //请开始你的表演
            }
        }
    }
![image](https://github.com/dylan0755/PhotoPicker/blob/master/gif/video.gif)