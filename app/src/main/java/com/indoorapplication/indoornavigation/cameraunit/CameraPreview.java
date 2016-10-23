package com.indoorapplication.indoornavigation.cameraunit;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.indoorapplication.indoornavigation.config.Constant;

import java.io.IOException;

import static android.Manifest.permission_group.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by liyuanqing on 2016/10/12.
 */

public class CameraPreview extends TextureView implements
        TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;
    private CaptureRequest.Builder mCaptureBuilder;
    private CameraCaptureSession mSession;
    private Context context;
    public CameraPreview(Context context) {
        super(context);
        //mCamera = camera;
        this.context = context;
        this.setSurfaceTextureListener(this);
        // TODO Auto-generated constructor stub
    }


    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
//        mCamera = Camera.open();
//        if(!isCameraGranted()){
//            return;
//        }
        try {
            mCamera = Camera.open();
            //mCamera = null;
        }catch (Exception e){
            Log.e("Exception",e.toString()+"");
            Toast.makeText(context,"Camara bug",Toast.LENGTH_SHORT).show();
            return;
        }
        Camera.Parameters objParam = mCamera.getParameters();
        objParam.setPreviewSize(Constant.height,Constant.width);
        //设置对焦模式为持续对焦，（最好先判断一下手机是否有这个对焦模式，有些手机没有会报错的）
        objParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        // m_objCamera.setDisplayOrientation(90);
        mCamera.setParameters(objParam);
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
//        this.setLayoutParams(new FrameLayout.LayoutParams(
//                (int) (previewSize.width*density),(int) (previewSize.height*density), Gravity.CENTER));
        this.setLayoutParams(new FrameLayout.LayoutParams(
                previewSize.width,previewSize.height, Gravity.CENTER));
//        this.setLayoutParams(new FrameLayout.LayoutParams(
//                Constant.height,Constant.width, Gravity.CENTER));
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        //实现自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    //initCamera();//实现相机的参数初始化
                    //camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            }

        });
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }

   /* //相机参数的初始化设置
    private void initCamera()
    {
        parameters=camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        setDispaly(parameters,camera);
        camera.setParameters(parameters);
        camera.startPreview();
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上

    }
*/
}