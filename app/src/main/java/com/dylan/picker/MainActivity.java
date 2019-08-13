package com.dylan.picker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dylan.photopicker.api.PhotoView;
import com.dylan.photopicker.app.PhotoPickerActivity;

public class MainActivity extends AppCompatActivity {
   private TextView tvSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSelected= (TextView) findViewById(R.id.tv_selected);
    }




    //本地相册
    public void openPicker(View view) {
        Intent intent = new Intent(view.getContext(), PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerActivity.EXTRA_MAX_SELECT,3);
        intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA_MULTI_CHOICE,false);//多选的时候显示相机入口
        //intent.putExtra(PhotoPickerActivity.EXTRA_NEED_SHOW_SELECT_LIMIT,true);
       // intent.putExtra(PhotoPickerActivity.EXTRA_LIMIT_SELECT_TIP,"不可以再选了");
        startActivityForResult(intent, 1001);
    }



    public void openOnLinePicShow(){

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            if (data==null)return;
            String[] array = data.getStringArrayExtra(PhotoPickerActivity.IMAGE_SELECT_ARRAY);
            if (array!=null){
                StringBuffer buffer=new StringBuffer();
                for (String str : array) {
                    Log.e("startActivityForResult", "str " + str);
                    buffer.append(str+"\n"+"\n");
                }
                tvSelected.setText(buffer.toString());
            }

        }
    }
}
