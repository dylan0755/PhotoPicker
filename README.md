# PhotoPicker

Intent intent = new Intent(view.getContext(), PhotoPickerActivity.class);
intent.putExtra(PhotoPickerActivity.EXTRA_MAX_SELECT,3); //单选 传1，多选大于1即可
intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA_MULTI_CHOICE,false);//多选的时候显示相机入口
//intent.putExtra(PhotoPickerActivity.EXTRA_NEED_SHOW_SELECT_LIMIT,true);
//intent.putExtra(PhotoPickerActivity.EXTRA_LIMIT_SELECT_TIP,"最多只能选9张图片哦");

startActivityForResult(intent, 1001);