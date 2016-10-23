package com.indoorapplication.indoornavigation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.im.data.IMDataManager;
import com.amap.api.im.listener.IMMapEventListener;
import com.amap.api.im.listener.IMMapLoadListener;
import com.amap.api.im.listener.MapLoadStatus;
import com.amap.api.im.util.IMLog;
import com.amap.api.im.util.IMPoint;
import com.amap.api.im.util.IMSearchResult;
import com.amap.api.im.view.IMIndoorMapFragment;
import com.autonavi.indoor.constant.Configuration;
import com.indoorapplication.indoornavigation.cameraunit.CameraPreview;
import com.indoorapplication.indoornavigation.config.Constant;
import com.indoorapplication.indoornavigation.unit.CommonUtil;
import com.indoorapplication.indoornavigation.unit.CrashHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements SensorEventListener {


    //相机相关参数
    public static final int MEDIA_TYPE_IMAGE = 1;
    TextView txtOrientationTextView;
    private SensorManager sensorManager;

    private float[] accelValues = new float[3];
    private float[] geoMagData = new float[3];
    private float[] orientationData = new float[3];
    private boolean isReady = false;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    //private Camera mCamera;
    private CameraPreview mPreview;
    private static final String TAG = "ERROR";

    private IMIndoorMapFragment mIndoorMapFragment = null;
    IMDataManager dataManager = IMDataManager.getInstance();
    private Context mContext = null;
    private ImageView mCustomImage = null;
    private IMDataManager mDataManager = null;
    String defaultBuilding = "B0FFFAB6J2"; //B0FFFAB6J2  B000A80ZU6
    FrameDrawOverRunnable mFrameDrawOverRunnable = null;

    private LinearLayout layout_map;
    private View view_map;
    private ImageView img_close;
    private boolean flag = true;

    private String mLastSelectedPoiId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        setContentView(R.layout.activity_main);

        CrashHandler crashHandler = CrashHandler.instance();
        crashHandler.init();
        //初始化相机
        // check Android 6 permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST","Granted");
            //init(barcodeScannerView, getIntent(), null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1);//1 can be another integer
        }
        CommonUtil.initDisplayMetrics(this);
        initCamera();
        initIndoorMap();
    }

    /**
     * 初始化室内地图
     * */
    private void initIndoorMap() {
        mDataManager = IMDataManager.getInstance();
//        mIndoorMapFragment =  (IMIndoorMapFragment)getSupportFragmentManager()
//                .findFragmentById(R.id.indoor_main_map_view);
        mIndoorMapFragment = (IMIndoorMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.indoor_main_map_view);

        layout_map = (LinearLayout) findViewById(R.id.layout_map);
        view_map = findViewById(R.id.view_map);
        img_close = (ImageView) findViewById(R.id.img_close);

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.width = (int)(100*Constant.density);
                layoutParams.height = (int)(100*Constant.density);
                mIndoorMapFragment.hideAmapLogo();
                mIndoorMapFragment.hideCompassView();
                mIndoorMapFragment.hideFloorView();
                mIndoorMapFragment.hideZoomView();
                mIndoorMapFragment.hidePlottingScale();
                flag = true;
                layout_map.setLayoutParams(layoutParams);
                img_close.setVisibility(View.GONE);

            }
        });

        view_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String id = mDataManager.getCurrentBuildingId();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if (flag) {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    mIndoorMapFragment.showAmapLogo();
                    mIndoorMapFragment.showCompassView();
                    mIndoorMapFragment.showFloorView();
                    mIndoorMapFragment.showZoomView();
                    mIndoorMapFragment.showPlottingScale();
                    flag = false;
                    layout_map.setLayoutParams(layoutParams);
                    img_close.setVisibility(View.VISIBLE);
                }
// else {
//                    layoutParams.width = (int)(150 * Constant.density);
//                    layoutParams.height = (int)(150 * Constant.density);
//                    mIndoorMapFragment.hideAmapLogo();
//                    mIndoorMapFragment.hideCompassView();
//                    mIndoorMapFragment.hideFloorView();
//                    mIndoorMapFragment.hideZoomView();
//                    flag = true;
//                }

               // Toast.makeText(mContext, id, Toast.LENGTH_SHORT).show();
            }
        });
        IMDataManager.setRequestTimeOut(5000);
//        mDataManager.setDataPath(Environment.getExternalStorageDirectory() + "/test_data");
//        mDataManager.downloadBuildingData(mBuildingId, mDataDownloadListener);

        //mDataManager.setDataPath(Environment.getExternalStorageDirectory() + "/test_data");
        //mIndoorMapFragment.setDataPath(Environment.getExternalStorageDirectory() + "/test_data");
        mIndoorMapFragment.loadMap(defaultBuilding, mMapLoadListener);//B023B173VP
        mIndoorMapFragment.setMapEventListener(mMapEventListener);
        mIndoorMapFragment.initSwitchFloorToolBar();
        mIndoorMapFragment.initZoomView();
        mIndoorMapFragment.initCompass();
        mIndoorMapFragment.initPlottingScale();

        mIndoorMapFragment.hideAmapLogo();
        mIndoorMapFragment.hideCompassView();
        mIndoorMapFragment.hideFloorView();
        mIndoorMapFragment.hideZoomView();
        mIndoorMapFragment.hidePlottingScale();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
    初始化摄像头
    */
    private void initCamera() {

//        try {
//            mCamera = getCameraInstance();
//        }catch (Exception e){
//            Toast.makeText(mContext,"摄像头问题", Toast.LENGTH_SHORT).show();
//            return;
//        }

        txtOrientationTextView = (TextView) findViewById(R.id.txtOrientationTextView);
        // Get the SensorManager instance
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 创建Preview view并将其设为activity中的内容
        mPreview = new CameraPreview(this);
        mPreview.setSurfaceTextureListener(mPreview);
        //设置浑浊
        mPreview.setAlpha(1.0f);
        mPreview.setRotation(90.0f);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        // preview.setAlpha(0.0f);
        preview.addView(mPreview);
    }


    /**
     * 地图加载回调接口
     */
    private IMMapLoadListener mMapLoadListener = new IMMapLoadListener() {

        @Override
        public void onMapLoadSuccess() {
            Toast.makeText(mIndoorMapFragment.getActivity(), "地图加载完毕",
                    Toast.LENGTH_LONG).show();
            if (mDataManager.getCurrentBuildingId().equals("B000A80ZU6")) //B023B173VP B000A80ZU6
            {
//                AssetManager asset=mContext.getAssets();
//                InputStream input = null;
//                try {
//                    input = asset.open("shpdata.bin");
//                    int len = input.available();
//                    byte[] buffer = new byte[len];
//                    input.read(buffer);
//                    input.close();
//                    mDataManager.setExtensionData(buffer);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (mDataManager.getCurrentFloorNo() == 1 && mCustomImage == null) {
                    mCustomImage = new ImageView(mContext);
                    mCustomImage.setImageResource(R.drawable.compass);
                    RelativeLayout rootlayout = (RelativeLayout) findViewById(R.id.rootlayout);
                    rootlayout.addView(mCustomImage);
                    mFrameDrawOverRunnable = new FrameDrawOverRunnable();
                }
            } else {
                if (mCustomImage != null) {
                    RelativeLayout rootlayout = (RelativeLayout) findViewById(R.id.rootlayout);
                    rootlayout.removeView(mCustomImage);
                    mCustomImage = null;
                    mFrameDrawOverRunnable = null;
                }
            }
            //initLocating(Configuration.LocationProvider.WIFI);

        }

        @Override
        public void onMapLoadFailure(MapLoadStatus mapLoadStatus) {
            //IMLog.logd("#######-------- onMapLoadFailure:" + mapLoadStatus + ", id:" + Thread.currentThread().getId());
            Toast.makeText(mIndoorMapFragment.getActivity(), "地图加载失败,失败状态:" + mapLoadStatus,
                    Toast.LENGTH_LONG).show();
        }

    };

    private IMMapEventListener mMapEventListener = new IMMapEventListener() {
        @Override
        public void onMarkerClick(String sourceID) {
            Toast.makeText(mContext, "选中自定义图标：" + sourceID, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFrameDrawOver() {
            if (mCustomImage != null && mFrameDrawOverRunnable != null)
                mCustomImage.post(mFrameDrawOverRunnable);
        }

        @Override
        public void onFloorChange(int floorNo) {
            //IMLog.logd("#######-------- onFloorChange id:" + Thread.currentThread().getId());
        }

        @Override
        public void onSelectedPoi(String poiId) {

            // IMLog.logd("#######-------- onSelectedShop:" + poiId + " id:" + Thread.currentThread().getId());

            if (poiId != null && !poiId.equals("")) {
                IMLog.logd("sourceIasdfsadsasad" + poiId);
                if (mLastSelectedPoiId != null) {
                    mIndoorMapFragment.clearFeatureColor(mLastSelectedPoiId);
                }
                mLastSelectedPoiId = poiId;

                mIndoorMapFragment.selectFeature(poiId);//气泡

                mIndoorMapFragment.setFeatureColor(poiId, "0x00000000");//高亮
                mIndoorMapFragment.refreshMap();
                //mIndoorMapFragment.setFeatureCenter(poiId);//居中
                String toasttext = "";
                toasttext += "PoiId:" + poiId;

                IMSearchResult searchresult = mDataManager.searchByID(poiId);
                if (searchresult != null) {
                    if (searchresult.getName() != null) {
                        toasttext += "\n" + "PoiName:" + searchresult.getName();
                    }
                    if (searchresult.getCatagory() != null) {
                        toasttext += "\n" + "基本分类:" + searchresult.getCatagory();
                        ArrayList<String> cats = new ArrayList<String>();
                        cats.add(searchresult.getCatagory());
                        toasttext += "\n" + "本类型个数:" + mDataManager.searchByCategories(cats, mDataManager.getCurrentFloorNo()).size();
                    }
                }
                Toast.makeText(mContext, toasttext, Toast.LENGTH_SHORT).show();

            } else {
                // 取消选择
//                mIndoorMapFragment.clearSelected();
//                mIndoorMapFragment.clearHighlight();
                //mLastSelectedPoiId = "";
            }

//            // 再路算选点界面下,不显示去这里按钮
//            if (mPathFragment == null || !mPathFragment.isPoiSelect()) {
//                mBottomViewDetail.setVisibility(View.VISIBLE);
//                mTextPoi.setText(poiId);
//            }
        }

        @Override
        public void onSingleTap(double lng, double lat) {
            IMLog.logd("#######-------- onSingleTap lng:" + lng + ", lat:" + lat
                    + " id:" + Thread.currentThread().getId());
            IMPoint cvtPoint = mIndoorMapFragment.convertCoordinateToScreen(lng, lat);
            IMLog.logd("#######-------- onSingleTap posX:" + cvtPoint.getX() + ", poxY:" + cvtPoint.getY()
                    + " id:" + Thread.currentThread().getId());
        }

        @Override
        public void onDoubleTap() {
            //IMLog.logd("#######-------- onDoubleTap id:" + Thread.currentThread().getId());
            mIndoorMapFragment.zoomIn();
        }

        @Override
        public void onLongPress() {
            //IMLog.logd("#######-------- onLongPress id:" + Thread.currentThread().getId());
        }

        @Override
        public void onInclineBegin() {

        }

        @Override
        public void onIncline(float centerX, float centerY, float shoveAngle) {

        }

        @Override
        public void onInclineEnd() {

        }

        @Override
        public void onScaleBegin() {
            //IMLog.logd("#######-------- onScaleBegin id:" + Thread.currentThread().getId());
        }

        @Override
        public void onScale(float focusX, float focusY, float scaleValue) {
//            IMLog.logd("#######-------- onRotate x:" + focusX + ", y:" + focusY + ", value:"
//                    + scaleValue);
        }

        @Override
        public void onScaleEnd() {
            //IMLog.logd("#######-------- onScaleEnd id:" + Thread.currentThread().getId());
        }

        @Override
        public void onTranslateBegin() {
            //IMLog.logd("#######-------- onTranslateBegin id:" + Thread.currentThread().getId());
        }

        @Override
        public void onTranslate(float transX, float transY) {
//            IMLog.logd("#######-------- onTranslate x:" + transX + ", y:" + transY);
        }

        @Override
        public void onTranslateEnd() {
            //IMLog.logd("#######-------- onTranslateEnd id:" + Thread.currentThread().getId());
        }

        @Override
        public void onRotateBegin() {
            //IMLog.logd("#######-------- onRotateBegin id:" + Thread.currentThread().getId());
        }

        @Override
        public void onRotate(float centerX, float centerY, float rotateAngle) {
//            IMLog.logd("#######-------- onRotate x:" + centerX + ", y:" + centerY + ", value:"
//                    + rotateAngle);
            mIndoorMapFragment.getMapRotation();
        }

        @Override
        public void onRotateEnd() {
            //IMLog.logd("#######-------- onRotateEnd id:" + Thread.currentThread().getId());
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                for(int i=0; i<3; i++){
                    accelValues[i] =  event.values[i];
                }
                if(geoMagData[0] != 0)
                    isReady = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                for(int i=0; i<3; i++){
                    geoMagData[i] =  event.values[i];
                }
                if(accelValues[2] != 0)
                    isReady = true;
                break;
            default:
                return;
        }

        if(!isReady) return;

        float R[] = new float[9];
        float I[] = new float[9];

        if (SensorManager.getRotationMatrix(R, I, accelValues, geoMagData)) {
            SensorManager.getOrientation(R, orientationData);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Azimuth: "+ Math.toDegrees(orientationData[0]));
            stringBuilder.append("\nPitch: "+ Math.toDegrees(orientationData[1]));
            stringBuilder.append("\nRoll: "+ Math.toDegrees(orientationData[2]));
            txtOrientationTextView.setText(stringBuilder.toString());
            if(-20 < Math.toDegrees(orientationData[2])&& Math.toDegrees(orientationData[2]) < 20){
                txtOrientationTextView.setTextColor(Color.parseColor("#ffffff"));
            }else{
                txtOrientationTextView.setTextColor(Color.parseColor("#FF4081"));

            }
        }
        else {
            txtOrientationTextView.setText("Failed to get rotation Matrix");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class FrameDrawOverRunnable implements Runnable {
        @Override
        public void run() {
            if (mCustomImage != null && mIndoorMapFragment != null) {
                IMPoint pt = mIndoorMapFragment.convertCoordinateToScreen(120.10769851677900, 30.299475678357201);
                mCustomImage.layout((int) pt.getX() - mCustomImage.getWidth() / 2,
                        (int) pt.getY() - mCustomImage.getHeight() / 2,
                        (int) pt.getX() + mCustomImage.getWidth() / 2,
                        (int) pt.getY() + mCustomImage.getHeight() / 2);
            }
        }

    }


    /**
     * 安全获取Camera对象实例的方法
     */

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // 试图获取Camera实例
        } catch (RuntimeException e) {
            // 摄像头不可用（正被占用或不存在）
        }
        return c; // 不可用则返回null
    }


}
