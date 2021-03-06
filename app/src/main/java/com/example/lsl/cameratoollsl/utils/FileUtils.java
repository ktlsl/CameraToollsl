package com.example.lsl.cameratoollsl.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件工具类
 * Created by lsl on 2017/10/13.
 */

public class FileUtils {
    private FileUtils(){
        /* cannot be instantiated */
    }
    /**
     * 保存图片
     *
     * @param data
     * @throws IOException
     */
    public static String savePic(byte[] data) throws IOException {
        File pics = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "images");
        if (!pics.exists()) {
            pics.mkdirs();
        }
        File file = new File(pics, System.currentTimeMillis() + ".jpg");
        Log.e("info---ssssss->", file.getAbsolutePath());
//        FileOutputStream fileOutputStream = new FileOutputStream(file);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        LogUtil.i("info--->", "图片的宽高:" + bitmap.getWidth() + "---:" + bitmap.getHeight());
        //图像翻转
//        bitmap = ImgUtil.getScale(bitmap, 1080, 1650);

//        Bitmap newBitmap = ImgUtil.setRotate(bitmap, 90f);

        saveFile(bitmap, file);
//        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

        bitmap.recycle();
//        newBitmap.recycle();

        return file.getAbsolutePath();
    }

    /**
     * 保存文件
     *
     * @param bitmap
     * @return
     */
    public static String saveFile(Bitmap bitmap, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.close();
        return file.getAbsolutePath();
    }

    /**
     * 保存bitmap
     *
     * @param bitmap
     * @return
     */
    public static String saveBitmap(Bitmap bitmap) throws IOException {
        File pics = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "images");
        if (!pics.exists()) {
            pics.mkdirs();
        }
        File file = new File(pics, System.currentTimeMillis() + ".jpg");
        String path = saveFile(bitmap, file);
        return path;
    }


}
