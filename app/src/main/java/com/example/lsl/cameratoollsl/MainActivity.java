package com.example.lsl.cameratoollsl;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lsl.cameratoollsl.utils.CameraUtil;
import com.example.lsl.cameratoollsl.utils.FileUtils;
import com.example.lsl.cameratoollsl.utils.ImgUtil;
import com.example.lsl.cameratoollsl.utils.ScreenUtils;
import com.example.lsl.cameratoollsl.widget.CameraPreView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lsl on 17-10-14.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private TextView mCapturetextView;//框框选择弹出按钮
    private ImageView mThumbimageView;    //缩略图
    private Button mTakePickbutton;  //拍照按钮
    private Button mAdd, mDel;  //扩大,缩小按钮

    //预览界面
    private CameraPreView mPreView;

    //相机对象
    private Camera mCamera;
    private SurfaceHolder mHolder;

    private Context mContext;

    private boolean isFocus; //对焦是否完毕

    private String mPath;


    private final String[] captures = {"无", "正方形", "长方形", "圆形"};


    private final String TAG = "info----->";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        iniView();
    }


    private void iniView() {
        mPreView = (CameraPreView) findViewById(R.id.camera_pre);
        mCapturetextView = (TextView) findViewById(R.id.capture_area);
        mThumbimageView = (ImageView) findViewById(R.id.thumb);
        mTakePickbutton = (Button) findViewById(R.id.takepick);
        mAdd = (Button) findViewById(R.id.capture_add);
        mDel = (Button) findViewById(R.id.capture_del);

        mAdd.setOnClickListener(this);
        mDel.setOnClickListener(this);
        mThumbimageView.setOnClickListener(this);
        mTakePickbutton.setOnClickListener(this);
        mCapturetextView.setOnClickListener(this);

        mHolder = mPreView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPreView.setOnTouchFocusListener(new CameraPreView.onTouchFocusListener() {
            @Override
            public void focus(Point point) {
                Log.e(TAG, "触摸回调了" + point.toString());
                focusOnTouch(point);
            }
        });

    }

    private void iniCamera() {
        if (!CameraUtil.hasCameraDevices(this)) {
            Toast.makeText(this, "设备没有可用相机", Toast.LENGTH_LONG).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
        } else {
            try {
                mCamera = CameraUtil.getCamera();
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Toast.makeText(this, "camera open faild", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }
    }

    private void setCameraParams() {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        mCamera.cancelAutoFocus();
        mCamera.setDisplayOrientation(90);//预览画面翻转90°

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(90); //输出的图片翻转90°
        parameters.setJpegQuality(100);
        parameters.setPictureSize(1280, 720);
        parameters.setPreviewSize(1280, 720);
        if (CameraUtil.isAutoFocusSuppored(parameters)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size s : sizeList) {
            Log.e(TAG, "支持尺寸:" + s.width + "X" + s.height);
        }
//        float radio = ScreenUtils.getScreenWidth(mContext) / ScreenUtils.getScreenHeight(mContext);
//        Log.e(TAG, "屏幕比例:" + radio);
//        Camera.Size size = CameraUtil.getPreviewSize(sizeList, radio);
//        Log.e(TAG, "计算后得到比例:" + size.width + " " + size.height);
//        parameters.setPreviewSize(size.width, size.height);
//        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }


    private void takePicture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {

                isFocus = b;
                if (b) {
                    mCamera.cancelAutoFocus();
                    mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            try {
                                takePicture2(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                mCamera.startPreview();
                            }
                            isFocus = false;
                        }
                    });
                }
            }
        });
    }

    /**
     * 拍照
     *
     * @param data
     * @throws IOException
     */
    public void takePicture2(byte[] data) throws IOException {
        if (mPreView.getCropMode() == CameraPreView.CropMode.NORMAL) {
            mPath = FileUtils.savePic(data);
        } else {
            int mode = 0;
            if (mPreView.getCropMode() == CameraPreView.CropMode.CIRCLE) {
                mode = 1;
            }
            Bitmap bitmap = ImgUtil.getCropBitmap(data, mPreView.getRect(), mPreView.getWidth(), mPreView.getHeight(), mode);
            mPath = FileUtils.saveBitmap(bitmap);
        }
        Bitmap thumb = ImgUtil.getThumbBitmap(mPath, ScreenUtils.dp2px(mContext, 50), ScreenUtils.dp2px(mContext, 50));
        mThumbimageView.setImageBitmap(thumb);
    }

    /**
     * 触摸对焦
     *
     * @param point
     */
    private void focusOnTouch(Point point) {
        if (mCamera == null) return;
        mCamera.cancelAutoFocus();
        Rect rect = CameraUtil.calculateTapArea(point.x, point.y, 1.0f, mPreView.getWidth(), mPreView.getHeight());
        Log.e(TAG, "对焦区域" + rect.toString());
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Area> areas = new ArrayList<>();
        areas.add(new Camera.Area(rect, 800));
        parameters.setFocusAreas(areas);
        mCamera.setParameters(parameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.e(TAG, "手动对焦成功" + success);
                Camera.Parameters param = mCamera.getParameters();
                if (CameraUtil.isAutoFocusSuppored(param)) {
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //设置会自动对焦模式
                    mCamera.setParameters(param);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture_area:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("选择框框形状");
                builder.setItems(captures, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //切换裁剪模式
                        setCropModel(which);
                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.thumb:
                Intent intent = new Intent(mContext, ThumbActivity.class);
                if (mPath != null) {
                    intent.putExtra("path", mPath);
                }
                startActivity(intent);
                break;
            case R.id.takepick:
                if (!isFocus)
                    takePicture();
                break;
            case R.id.capture_add:
                mPreView.zoomOut();
                break;
            case R.id.capture_del:
                mPreView.zoomIn();
                break;
        }
    }

    /**
     * 切换裁剪模式
     *
     * @param model
     */
    private void setCropModel(int model) {
        switch (model) {
            case 0:
                mPreView.setCropMode(CameraPreView.CropMode.NORMAL);
                break;
            case 1:
                mPreView.setCropMode(CameraPreView.CropMode.SQUARE);
                break;
            case 2:
                mPreView.setCropMode(CameraPreView.CropMode.RECTANGLE);
                break;
            case 3:
                mPreView.setCropMode(CameraPreView.CropMode.CIRCLE);
                break;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        iniCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        setCameraParams();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iniCamera();
                    setCameraParams();
                } else {
                    //faild
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }


//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }
}
