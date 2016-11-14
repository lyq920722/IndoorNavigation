package com.indoorapplication.indoornavigation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Path;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.im.data.IMDataManager;
import com.amap.api.im.data.IMRoutePlanning;
import com.amap.api.im.listener.DownloadStatusCode;
import com.amap.api.im.listener.IMDataDownloadListener;
import com.amap.api.im.listener.IMMapEventListener;
import com.amap.api.im.listener.IMMapLoadListener;
import com.amap.api.im.listener.IMRoutePlanningListener;
import com.amap.api.im.listener.MapLoadStatus;
import com.amap.api.im.listener.RoutePLanningStatus;
import com.amap.api.im.util.IMFloorInfo;
import com.amap.api.im.util.IMLog;
import com.amap.api.im.util.IMPoint;
import com.amap.api.im.util.IMSearchResult;
import com.amap.api.im.view.IMIndoorMapFragment;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.autonavi.indoor.constant.Configuration;
import com.autonavi.indoor.constant.MessageCode;
import com.autonavi.indoor.entity.LocationResult;
import com.autonavi.indoor.location.ILocationManager;
import com.autonavi.indoor.onlinelocation.OnlineLocator;
import com.google.gson.Gson;
import com.indoorapplication.indoornavigation.cameraunit.CameraPreview;
import com.indoorapplication.indoornavigation.config.Constant;
import com.indoorapplication.indoornavigation.http.RequestManager;
import com.indoorapplication.indoornavigation.route.PathFragment;
import com.indoorapplication.indoornavigation.route.PoiInfo;
import com.indoorapplication.indoornavigation.route.PoiMapCell;
import com.indoorapplication.indoornavigation.route.bean.GeometryEntity;
import com.indoorapplication.indoornavigation.route.bean.PathInfo;
import com.indoorapplication.indoornavigation.route.bean.RoadInfo;
import com.indoorapplication.indoornavigation.route.bean.RoadPathInfo;
import com.indoorapplication.indoornavigation.unit.CommonUtil;
import com.indoorapplication.indoornavigation.unit.CrashHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
    //IMDataManager dataManager = IMDataManager.getInstance();
    private Context mContext = null;
    private ImageView mCustomImage = null;
    private IMDataManager mDataManager = null;
    String defaultBuilding = "B000A80ZU6"; //B0FFFAB6J2  B000A80ZU6 B000A856LJ
    FrameDrawOverRunnable mFrameDrawOverRunnable = null;
    private LinearLayout layout_map;
    private View view_map;
    private ImageView img_close;
    private boolean flag = true;

    private String mLastSelectedPoiId = null;


    //private PathFragment mPathFragment = null;
    private ImageView mLocationView = null;
    private boolean mLocationStatus = false;                // 定位按钮状态

    private Button btn_route;
    private Button btn_route_clear;
    private IMPoint mLastPoint = null;
    private List<GeometryEntity> geometry;
    private List<RoadPathInfo.RoadEntity> road;

    private PoiInfo mInfoFrom;
    private PoiInfo mInfoTo;

    private  Button btn_istest;
    private boolean istest = true;  //是否测试模式
    private IMPoint mLocationPoint = null;
    private String mLocationBdId = "";
    private StringBuilder stringBuilder = new StringBuilder();
    private double distance_ab = 0.0; //前点距离
    private double distance_ac = 0.0; //后点距离
    private double distance_bc = 0.0; //线段距离
    public static double THRESHOLD_WAY = 5.0;  //是否在路上阈值
    public static double THRESHOLD_POINT = 3.0; //是否到达阈值
    private boolean isNavi = true; //是否导航状态
    private boolean isOnway = false; //是否在路上状态
    private int index = 0; //导航数列进行下标
    private String mentionInfo = "";     //导航主提示内容
    private String subText = ""; //导航副提示内容
    private double endDistance; //距离拐点距离

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        setContentView(R.layout.activity_main);
        //crash收集
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
        initView();
        initLocationButton();
        initCamera();
        initIndoorMap();
        RequestManager.init(mContext);
        //requestPath();
       /* mPathFragment = PathFragment.newInstance(this, mIndoorMapFragment);
        //mPathFragment.setSearchEditText(mSearchEditText);
        mPathFragment.setLocationView(mLocationView);*/
    }

    /**
     * 初始化控件
     */
    private void initView(){
        mLocationView = (ImageView)findViewById(R.id.locating_btn);  //定位按钮
        txtOrientationTextView = (TextView) findViewById(R.id.txtOrientationTextView);
        layout_map = (LinearLayout) findViewById(R.id.layout_map);
        view_map = findViewById(R.id.view_map);
        img_close = (ImageView) findViewById(R.id.img_close);
        btn_route = (Button) findViewById(R.id.btn_route);
        btn_route_clear = (Button) findViewById(R.id.btn_route_clear);
        btn_istest = (Button) findViewById(R.id.btn_istest);
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

        btn_istest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(istest){
                    istest = false;
                    isNavi = true;
                    btn_istest.setText("使用模拟定位");
                    mIndoorMapFragment.clearLocatingPosition();
                    startLocating();
                    mLocationView.setImageResource(R.drawable.indoor_gps_locked);
                    mLocationStatus = false;
                    mFirstCenter = false;
                    txtOrientationTextView.setText("我的位置："+"");
                    mIndoorMapFragment.refreshMap();
                }else{
                    istest = true;
                    isNavi = false;
                    btn_istest.setText("使用真实定位");
                    stopLocating();
                    txtOrientationTextView.setText("我的位置： x:"+"116.3907"+"   y:" +"39.993217");
                    ///mIndoorMapFragment.setLocatingPosition(116.3907, 39.993217, 1, values[0], 1);
                }
            }
        });

        btn_route_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIndoorMapFragment.clearRouteResult();
                txtOrientationTextView.setText("....");
            }
        });

        btn_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planningRoad();
                //btnGoHere();
            }
        });

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
                btn_route.setVisibility(View.GONE);
                btn_route_clear.setVisibility(View.GONE);
                mLocationView.setVisibility(View.GONE);
                btn_istest.setVisibility(View.GONE);
                txtOrientationTextView.setText("Values");
                stopLocating();

            }
        });

        view_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String id = mDataManager.getCurrentBuildingId();
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if (flag) {
                    istest = true;
                    btn_istest.setText("使用真实定位");
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    mIndoorMapFragment.showAmapLogo();
                    mIndoorMapFragment.showCompassView();
                    //mIndoorMapFragment.showFloorView();
                    mIndoorMapFragment.showZoomView();
                    mIndoorMapFragment.showPlottingScale();
                    flag = false;
                    layout_map.setLayoutParams(layoutParams);
                    img_close.setVisibility(View.VISIBLE);
                    btn_route.setVisibility(View.VISIBLE);
                    btn_route_clear.setVisibility(View.VISIBLE);
                    mLocationView.setVisibility(View.VISIBLE);
                    btn_istest.setVisibility(View.VISIBLE);

                }
            }
        });
        IMDataManager.setRequestTimeOut(5000);
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
            initLocating(Configuration.LocationProvider.BLE); //蓝牙定位
            //initLocating(Configuration.LocationProvider.WIFI);

        }

        @Override
        public void onMapLoadFailure(MapLoadStatus mapLoadStatus) {
            //IMLog.logd("#######-------- onMapLoadFailure:" + mapLoadStatus + ", id:" + Thread.currentThread().getId());
            Toast.makeText(mIndoorMapFragment.getActivity(), "地图加载失败,失败状态:" + mapLoadStatus,
                    Toast.LENGTH_LONG).show();
        }

    };

    /**
     * 初始化定位按钮
     */
    private void initLocationButton() {

        mLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IMLog.logd("#######-------- onClick ");

                if (!mLocationStatus) {         // 处理开始定位
                    startLocating();
                    mLocationView.setImageResource(R.drawable.indoor_gps_locked);
                    mLocationStatus = true;
                    mFirstCenter = false;
                } else {                    // 处理结束定位
                    stopLocating();
                    mLocationView.setImageResource(R.drawable.indoor_gps_unlocked);
                    mIndoorMapFragment.clearLocatingPosition();
                    mIndoorMapFragment.clearLocationOnFloorView();
                    mIndoorMapFragment.refreshMap();
                    mLocationStatus = false;
                }

            }
        });
    }

    /**
     * 地图事件监听
     */
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

            }
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
            case Sensor.TYPE_ACCELEROMETER:   //TYPE_ORIENTATION  TYPE_ACCELEROMETER
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

//        if (SensorManager.getRotationMatrix(R, I, accelValues, geoMagData)) {
//            SensorManager.getOrientation(R, orientationData);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("Azimuth: "+ Math.toDegrees(orientationData[0]));
//            stringBuilder.append("\nPitch: "+ Math.toDegrees(orientationData[1]));
//            stringBuilder.append("\nRoll: "+ Math.toDegrees(orientationData[2]));
//            //txtOrientationTextView.setText(stringBuilder.toString());
//            if(-20 < Math.toDegrees(orientationData[2])&& Math.toDegrees(orientationData[2]) < 20){
//                txtOrientationTextView.setTextColor(Color.parseColor("#ffffff"));
//            }else{
//                txtOrientationTextView.setTextColor(Color.parseColor("#FF4081"));
//
//            }
//        }
//        else {
//            txtOrientationTextView.setText("Failed to get rotation Matrix");
//        }
        calculateOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * 下载回调接口（暂时不用）
     */
    /*private IMDataDownloadListener mDataDownloadListener = new IMDataDownloadListener() {

        @Override
        public void onDownloadSuccess(String buildingId) {
            // TODO Auto-generated method stub
            IMLog.logd("#######-------- download success:" + buildingId + ", id:" + Thread.currentThread().getId());
            //mDataManager.loadBuildingData();

        }

        @Override
        public void onDownloadFailure(String buildingId, DownloadStatusCode statusCode) {
            // TODO Auto-generated method stub
            IMLog.logd("#######-------- download failure:" + buildingId + ", errorCode:" +
                    statusCode + ", id:" + Thread.currentThread().getId());
        }

        @Override
        public void onDownloadProgress(String buildingId, float progress) {
            // TODO Auto-generated method stub
            IMLog.logd("####### building:" + buildingId + ", progress:" + progress + ", id:" + Thread.currentThread().getId());

        }

    };*/


    public Context getContext() {
        return mContext;
    }

    private boolean isExit = false;
    private void exit() {
        if(!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
        }
    }

    // ---------------------       定位SDK调用       -----------------------------
    boolean mIsManagerInited = false;
    boolean mIsLocating = false;
    SDKInitHandler mSDKInitHandler = new SDKInitHandler(this);;
    com.autonavi.indoor.constant.Configuration.Builder mConfigBuilder = null;
    private final InnerHandler mInnerHandler = new InnerHandler(this);
    public int mLastLocatedFloorNO = -99999;
    private ILocationManager mLocationManager = null;
    //ILocationManager mLocationManager;
    private static long mLastLocationTime = 0;
    private static int mLocationIntervalTime = 1000;
    private static boolean mLocationIntervalFlag = true;
    private boolean mFirstCenter = false;                       // 第一次定位居中

    public void initLocating(Configuration.LocationProvider provider) {
        IMLog.logd("#######init " + provider);

        //VERSION 5.5
        mConfigBuilder = new Configuration.Builder(mContext);
        String key = "";
        try {
            ApplicationInfo appInfo = mContext.getPackageManager()
                    .getApplicationInfo(mContext.getPackageName(),
                            PackageManager.GET_META_DATA);

            key = appInfo.metaData.getString("indoormap3d_key");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "定位Lbs Key错误", Toast.LENGTH_SHORT).show();
        }

        mConfigBuilder.setLBSParam(key);
        //mConfigBuilder.setSqlitePath(Environment.getExternalStorageDirectory() + "/autonavi/indoor/indoor_db.db");

//        //离线定位
//        mLocationManager = com.autonavi.indoor.location.LocationManager.getInstance();			//混合定位
//        //mConfigBuilder.setServer(Configuration.ServerType.SERVER_AOS, "");
//        mConfigBuilder.setLocationMode(Configuration.LocationMode.OFFLINE);

        //在线定位
        mLocationManager = OnlineLocator.getInstance();				//在线定位

        //mConfigBuilder.setServer(Configuration.ServerType.SERVER_AOS, "");
        //mConfigBuilder.setLocationMode(Configuration.LocationMode.AUTO);

        mSDKInitHandler = new SDKInitHandler(this);
        mConfigBuilder.setLocationProvider(provider);		//Configuration.LocationProvider.WIFI

        mLocationManager.init("",
                mConfigBuilder.build(), mSDKInitHandler);

//        mLocationManager.init(mIndoorMapFragment.getCurrentBuildingId(),
//                mConfigBuilder.build(), mSDKInitHandler);

    }


    private void destroyLocating(){
        IMLog.logd("#######destroy");
        if (mLocationManager != null) {
            IMLog.logd("#######destroy in");
            mLocationManager.destroy();
            mIsManagerInited = false;
            mLocationManager = null;
        }
    }

    void startLocating(){
        IMLog.logd("#######start");
        if (mIsManagerInited && !mLocationStatus && mLocationManager != null) {
            IMLog.logd("#######start in");

            //try {
            mLocationManager.requestLocationUpdates(mInnerHandler);
            //} catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            //	e.printStackTrace();
            //}

            //mIsLocating = true;
        }
    }
    void stopLocating(){
        IMLog.logd("#######stop");
        if (mIsManagerInited && mLocationManager != null) {
            IMLog.logd("#######stop in");
            mLocationManager.removeUpdates(mInnerHandler);
            mIsLocating = false;
        }
    }
    private class FrameDrawOverRunnable implements Runnable {
        @Override
        public void run() {
            if(mCustomImage!=null&&mIndoorMapFragment!=null) {
                IMPoint pt=mIndoorMapFragment.convertCoordinateToScreen(120.10769851677900,30.299475678357201);
                mCustomImage.layout((int) pt.getX()-mCustomImage.getWidth()/2,
                        (int)pt.getY()-mCustomImage.getHeight()/2 ,
                        (int) pt.getX()+mCustomImage.getWidth()/2,
                        (int)pt.getY()+mCustomImage.getHeight()/2);
            }
        }

    }


    private static class SDKInitHandler extends Handler {
        private final WeakReference<MainActivity> mParent;
        public SDKInitHandler(MainActivity parent) {
            mParent = new WeakReference<MainActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity parent = mParent.get();
            if (parent == null ) {
                IMLog.logd("#######parent == null");
                return;
            }
//			//parent.mIsLocating = false;
//			L.d("mIsLocating = false");
            if (msg.what == MessageCode.MSG_THREAD_PREPARED){
                IMLog.logd("#######Initialize LocationManager with Configuration");
                if (parent.mLocationManager == null) {
                    return;
                }

                parent.mIsManagerInited = true;
                //parent.mLocationManager.requestLocationUpdates(parent.mInnerHandler);
                parent.mIsLocating = true;
                //parent.start();
                IMLog.logd("#######mIsLocating = true");
            }
            else if (msg.what == MessageCode.MSG_WIFI_NOT_ENABLED ){
                Toast.makeText(parent.getContext(), "请先打开wifi", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_WIFI_NOT_PERMITTED ){
                Toast.makeText(parent.getContext(), "wifi没有授权", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_BLE_NOT_PERMITTED ){
                Toast.makeText(parent.getContext(), "BLE没有授权", Toast.LENGTH_SHORT).show();
            } else if (msg.what == MessageCode.MSG_BLE_NOT_ENABLED ){
                parent.txtOrientationTextView.setText("请先打开蓝牙");
                Toast.makeText(parent.getContext(), "请先打开BLE", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_SENSOR_MISSING ){
                Toast.makeText(parent.getContext(), "手机缺少步导需要的传感器：加速度、磁力计、重力计等", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_NETWORK_ERROR ){
                Toast.makeText(parent.getContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_NETWORK_NOT_SATISFY){
                Toast.makeText(parent.getContext(), "当前网络和用户设置的不符，不能下载数据", Toast.LENGTH_SHORT).show();
            }else if (msg.what == MessageCode.MSG_SERVER_ERROR){
                Toast.makeText(parent.getContext(), "服务器端错误", Toast.LENGTH_SHORT).show();
            } else {
                IMLog.logd("#######error!");
            }
        }
    };

    private static class InnerHandler extends Handler{
        private final WeakReference<MainActivity> mParent;
        public InnerHandler(MainActivity parent) {
            mParent = new WeakReference<MainActivity>(parent);
        }
        @Override
        public void handleMessage(Message msg)
        {
            MainActivity mParent = this.mParent.get();
            if (mParent == null) {
                IMLog.logd("#######2.00 parent is NULL");
                return;
            }

            switch (msg.what) {
                case -1: {
                    IMLog.logd("#######2.00");
                    break;
                }
                case MessageCode.MSG_REPORT_ONLINE_LOCATION: {
                    onLocated(msg, true);
                    break;
                }
                case MessageCode.MSG_REPORT_LOCATION: {
                    onLocated(msg, false);
                    break;
                }
                case MessageCode.MSG_SENSOR_MISSING: {
                    Toast.makeText(mParent.getContext(), "手机缺少步导需要的传感器：加速度、磁力计、重力计等", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageCode.MSG_BLE_NO_SCAN: {
                    Toast.makeText(mParent.getContext(), "一段时间内没有蓝牙扫描", Toast.LENGTH_SHORT).show();
                    mParent.txtOrientationTextView.setText("我的位置：正在定位...");
                    break;
                }
                case MessageCode.MSG_WIFI_NO_SCAN: {
                    Toast.makeText(mParent.getContext(), "一段时间内没有WIFI扫描", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageCode.MSG_NETWORK_ERROR: {
                    Toast.makeText(mParent.getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageCode.MSG_NETWORK_NOT_SATISFY: {
                    Toast.makeText(mParent.getContext(), "当前网络和用户设置的不符，不能下载数据", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageCode.MSG_SERVER_ERROR: {
                    Toast.makeText(mParent.getContext(), "服务器端错误", Toast.LENGTH_SHORT).show();
                    break;
                }case MessageCode.MSG_PRESSURE_CHANGED: {
                    //Toast.makeText(mParent.getContext(), "气压值改变异常", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MessageCode.MSG_REPORT_PED: {
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

        /**
         * 定位结果
         * @param msg
         * @param isOnline
         */
        void onLocated(Message msg, boolean isOnline){
            IMLog.logd("onLocated");
            MainActivity parent = mParent.get();
            if (parent == null)
                return;
            LocationResult result = (LocationResult)msg.obj;
            IMLog.logd("#######客户端收到定位结果：isOnline="+ isOnline + ", ("+result.x + ", " +result.y +","+ result.z +"),"+result.a);
            if (result.x == 0 && result.y == 0 && result.z == -99){
                IMLog.logd("#######无法定位 -99");
                return ;
            }
            if (result.x == -10000 || result.y == -10000 ){
                IMLog.logd("#######无法定位 -1000");
                return ;
            }
            if (result.x == 0 && result.y == 0 && result.z == -127){
                IMLog.logd("#######无法定位  -127");
                return ;
            }

            long curTime = System.currentTimeMillis();

            // 跳点模式
//            if (mLocationIntervalFlag && curTime - mLastLocationTime < mLocationIntervalTime) {
//                return;
//            }

            mLastLocationTime = curTime;

            IMLog.logd("#######lng:" + result.x + ", lat:"
                    + result.y + ", floor:"
                    + result.z + ", angel:"
                    + result.a + ", accuracy:"
                    + result.r);

//            if(parent.isNavi && parent.geometry != null && parent.geometry.size() > 0){
//                parent.navigation();
//            }

            if(parent.isNavi && parent.road != null && parent.road.size() > 0){
                parent.navigation_ARroad();
            }
//            parent.txtOrientationTextView.setText("我的位置：\n"+" lng:" + result.x + ", \nlat:"
//                    + result.y + ", \nfloor:"
//                    + result.z + ", \nangel:"
//                    + result.a + ", \naccuracy:"
//                    + result.r);
            if (!result.bid.equals(parent.mIndoorMapFragment.getCurrentBuildingId())) {
                Toast.makeText(parent.getContext(), "定位结果不在当前建筑物内!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 显示定位点
            parent.mIndoorMapFragment.setLocatingPosition(result.x, result.y, result.z,
                    result.a, result.r);

            if (parent.mIndoorMapFragment.getCurrentFloorNo() != result.z) {
                parent.mIndoorMapFragment.switchFloorByFloorNo(result.z);
            }

            if (!parent.mFirstCenter) {
                parent.mIndoorMapFragment.setCoordinateCenter(result.x, result.y, (int) result.z);
                parent.mIndoorMapFragment.setCoordinateDirect(result.a);
                parent.mIndoorMapFragment.setMapIncline(-45);
                //parent.mFirstCenter = true;
            }

            // 搜索定位周围的点
//            List<IMSearchResult> searchResultList = IMDataManager
//                    .getInstance().searchByDistance(result.x, result.y, result.z, 100, 20);
//
//            List<String> featureList = new ArrayList<String>();
//            for (IMSearchResult sr: searchResultList) {
//                    featureList.add(sr.getId());
//            }
//            parent.mIndoorMapFragment.selectSearchResultList(featureList);

            // 记录定位点
            IMPoint tmpPoint = new IMPoint(result.x, result.y, result.z);
            parent.mLastPoint = tmpPoint;
            parent.mLocationPoint = tmpPoint;
            parent.mLocationBdId = result.bid;
            /*parent.mPathFragment.setLocationPoint(tmpPoint);
            parent.mPathFragment.setLocationBdId(result.bid);
*/
            // 切层
            parent.mIndoorMapFragment.showLocationOnFloorView(result.z);

        }
    };


    /**
     * 方向计算
     */
    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelValues, geoMagData);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        Log.i(TAG, values[0] + "");
        /*if (values[0] >= -5 && values[0] < 5) {
            txtOrientationTextView.setText("正北");
        } else if (values[0] >= 5 && values[0] < 85) {
            // Log.i(TAG, "东北");
            txtOrientationTextView.setText("东北");
        } else if (values[0] >= 85 && values[0] <= 95) {
            // Log.i(TAG, "正东");
            txtOrientationTextView.setText("正东");
        } else if (values[0] >= 95 && values[0] < 175) {
            // Log.i(TAG, "东南");
            txtOrientationTextView.setText("东南");
        } else if ((values[0] >= 175 && values[0] <= 180)
                || (values[0]) >= -180 && values[0] < -175) {
            // Log.i(TAG, "正南");
            txtOrientationTextView.setText("正南");
        } else if (values[0] >= -175 && values[0] < -95) {
            // Log.i(TAG, "西南");
            txtOrientationTextView.setText("西南");
        } else if (values[0] >= -95 && values[0] < -85) {
            // Log.i(TAG, "正西");
            txtOrientationTextView.setText("正西");
        } else if (values[0] >= -85 && values[0] < -5) {
            // Log.i(TAG, "西北");
            txtOrientationTextView.setText("西北");
        }*/
        if(istest && !flag){

            //geometry = object.getRoute().get(0).getPath().getNaviInfoList().get(0).getGeometry();
           if(geometry != null && geometry.size()>0){
               stringBuilder = new StringBuilder();
               stringBuilder.append("获得途径点数：" + geometry.size()+"\n");
               stringBuilder.append("当前位置： x: "+geometry.get(0).getX()+"  y:" +(geometry.get(0).getY()-0.00036)+"\n");
               stringBuilder.append("是否在线内：" + "true" +"\n");
               stringBuilder.append("前点： x:"+geometry.get(1).getX()+"  y:" +geometry.get(1).getY()+"\n");
               stringBuilder.append("后点： x:"+geometry.get(2).getX()+"  y:" +geometry.get(2).getY()+"\n");
               stringBuilder.append("距离前点："+CommonUtil.formatData(CommonUtil.getDistance((geometry.get(0).getX()+0.00002),(geometry.get(0).getY()-values[0]/10000000),geometry.get(1).getX(),geometry.get(1).getY()))+"\n");
               stringBuilder.append("距离后点： x:"+CommonUtil.formatData(CommonUtil.getDistance((geometry.get(0).getX()+0.00002),(geometry.get(0).getY()-values[0]/10000000),geometry.get(2).getX(),geometry.get(2).getY()))+"\n");
               stringBuilder.append("前后点距离："+CommonUtil.formatData(CommonUtil.getDistance(geometry.get(1).getX(),geometry.get(1).getY(),geometry.get(2).getX(),geometry.get(2).getY()))+"\n");
               //txtOrientationTextView.setText(stringBuilder);

               //navigation();
           }


            mIndoorMapFragment.setLocatingPosition(116.3907, 39.993217, 1, values[0], 1);
        }
        //mIndoorMapFragment.setLocatingPosition(116.3907, 39.993217, 1, values[0], 1);
    }

    /**
     * 导航位置判断（线导）
     *
     */
    private void navigation() {
        if(index < geometry.size()){
            if(index == 0){
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index).getX(),geometry.get(index).getY());
                if(distance_ab < THRESHOLD_POINT){
                    isOnway = true;
                    index++;
                }else{
                    return;
                }
            }else if(index == geometry.size()-1){
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index-1).getX(),geometry.get(index-1).getY());
                distance_ac = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index).getX(),geometry.get(index).getY());
                distance_bc = CommonUtil.getDistance(geometry.get(index-1).getX(),geometry.get(index-1).getY(),geometry.get(index).getX(),geometry.get(index).getY());
                if(distance_ac < THRESHOLD_POINT){
                    isOnway = true;
                    isNavi = false;
                    Toast.makeText(mContext,"到达！",Toast.LENGTH_SHORT).show();
                }

            }else{
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index-1).getX(),geometry.get(index-1).getY());
                distance_ac = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index).getX(),geometry.get(index).getY());
                distance_bc = CommonUtil.getDistance(geometry.get(index-1).getX(),geometry.get(index-1).getY(),geometry.get(index).getX(),geometry.get(index).getY());
                if(distance_ab + distance_ac <= distance_bc+THRESHOLD_WAY){
                    isOnway = true;
                    if(distance_ac <= THRESHOLD_POINT){
                        index++;
                    }
                }else{
                    isOnway = false;
                    planningRoad();
                    //Toast.makeText(mContext,"偏离线路，重新规划",Toast.LENGTH_SHORT).show();
                }
            }

            stringBuilder = new StringBuilder();
            //stringBuilder.append("index：" + index+"\n");
            //stringBuilder.append("当前位置： x: "+mLastPoint.getX()+"  y:" +mLastPoint.getY()+"\n");
            stringBuilder.append("是否在线内：" + isOnway +"\n");
            //stringBuilder.append("前点： x:"+geometry.get(1).getX()+"  y:" +geometry.get(1).getY()+"\n");
            //stringBuilder.append("后点： x:"+geometry.get(2).getX()+"  y:" +geometry.get(2).getY()+"\n");
            stringBuilder.append("距离前点：" +distance_ab+"\n");
            stringBuilder.append("距离后点："+distance_ac+"\n");
            stringBuilder.append("前后点距离："+distance_bc+"\n");
            txtOrientationTextView.setText(stringBuilder);

        }
    }


    /**
     * 导航位置判断（AR导航）
     *
     */
    private void navigation_ARroad() {
        if(index < road.size()){
            endDistance = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                    road.get(index).getEndRoad().getX(),road.get(index).getEndRoad().getY());
            subText = road.get(index).getSubText();
            //endDistance = CommonUtil.formatData(endDistance);
            mentionInfo = road.get(index).getDesc();
            if(mentionInfo.contains("d")){
                mentionInfo = mentionInfo.replaceAll("d",CommonUtil.formatData(endDistance)+"");
            }

            if(index == 0){ //起点位置判断
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                        road.get(index).getPrev().getX(),road.get(index).getPrev().getY());
                if(distance_ab < THRESHOLD_POINT){
                    isOnway = true;
                    index++;
                }else{
                    return;
                }
            }else if(index == road.size()-1){ //终点到达判断
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                        road.get(index).getPrev().getX(),road.get(index).getPrev().getY());
                distance_ac = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                        road.get(index).getCurr().getX(),road.get(index).getCurr().getY());
                distance_bc = CommonUtil.getDistance(road.get(index).getPrev().getX(),
                        road.get(index).getPrev().getY(),
                        road.get(index).getCurr().getX(),
                        road.get(index).getCurr().getY());
                if(distance_ac < THRESHOLD_POINT){
                    isOnway = true;
                    isNavi = false;
                    Toast.makeText(mContext,"到达！",Toast.LENGTH_SHORT).show();
                    txtOrientationTextView.setText("已到达目的地");
                }

            }else{ //路程中判断
                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                        road.get(index).getPrev().getX(),road.get(index).getPrev().getY());
                distance_ac = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
                        road.get(index).getCurr().getX(),road.get(index).getCurr().getY());
                distance_bc = CommonUtil.getDistance(road.get(index).getPrev().getX(),
                        road.get(index).getPrev().getY(),
                        road.get(index).getCurr().getX(),
                        road.get(index).getCurr().getY());
//                distance_ab = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),
//                        geometry.get(index-1).getX(),geometry.get(index-1).getY());
//                distance_ac = CommonUtil.getDistance(mLastPoint.getX(),mLastPoint.getY(),geometry.get(index).getX(),geometry.get(index).getY());
//                distance_bc = CommonUtil.getDistance(geometry.get(index-1).getX(),geometry.get(index-1).getY(),geometry.get(index).getX(),geometry.get(index).getY());
                if(distance_ab + distance_ac <= distance_bc+THRESHOLD_WAY){
                    isOnway = true;
                    if(THRESHOLD_POINT-1 < distance_ac && distance_ac <= THRESHOLD_POINT){
                        mentionInfo = road.get(index).getDesc2();
                        if(distance_ac <= THRESHOLD_POINT){
                            mentionInfo = road.get(index).getDesc();
                            index++;
                        }
                        if(mentionInfo.contains("d")){
                            mentionInfo.replaceAll("d",CommonUtil.formatData(endDistance)+"");
                        }
                    }

                }else{
                    isOnway = false;
                    planningRoad();
                    //Toast.makeText(mContext,"偏离线路，重新规划",Toast.LENGTH_SHORT).show();
                }
            }

            stringBuilder = new StringBuilder();
            stringBuilder.append("主提示：" + mentionInfo +"\n");
            stringBuilder.append("副提示：" + subText +"\n\n");
            stringBuilder.append("该路共有点：" + road.size()+"个\n");
            stringBuilder.append("当前已经过：" + index+"号点\n");
            //stringBuilder.append("当前位置： x: "+mLastPoint.getX()+"  y:" +mLastPoint.getY()+"\n");
            stringBuilder.append("是否在线内：" + isOnway +"\n");
            //stringBuilder.append("前点： x:"+geometry.get(1).getX()+"  y:" +geometry.get(1).getY()+"\n");
            //stringBuilder.append("后点： x:"+geometry.get(2).getX()+"  y:" +geometry.get(2).getY()+"\n");
            stringBuilder.append("距离前点：" +distance_ab+"\n");
            stringBuilder.append("距离后点："+distance_ac+"\n");
            stringBuilder.append("前后点距离："+distance_bc+"\n");
            txtOrientationTextView.setText(stringBuilder);

        }
    }

    /**
     * 路径起终点设置
     */
    public void setBegin(){
        index = 0; //导航位置归零

        PoiInfo tmpPoiInfo = new PoiInfo();
        tmpPoiInfo.PoiInfoType = Constant.TYPE_ROUTE_PLANNING_POI;
        String curSelectId = "ZH0000300210100051"; //ZH0000300210100152 ZH0000300210100024
        int floorNo = 1;
        IMFloorInfo tmpFloorInfo = new IMFloorInfo(floorNo, "");
        PoiMapCell tmpPoiMapCell = new PoiMapCell();
        tmpPoiMapCell.setFloorNo(floorNo);
        tmpPoiMapCell.setPoiId(curSelectId);
        tmpPoiMapCell.setName(curSelectId);
        tmpPoiInfo.cell = tmpPoiMapCell;
        tmpPoiInfo.floor = tmpFloorInfo;
        //mPathFragment.setPoiInfoFrom(tmpPoiInfo);
        mInfoTo = tmpPoiInfo;

        PoiInfo tmpPoiInfo_from = new PoiInfo();
        if(mLastPoint != null && !istest){
            tmpPoiInfo_from.PoiInfoType = Constant.TYPE_ROUTE_PLANNING_LOCATION;
            IMFloorInfo tmpFloorInfo1 = new IMFloorInfo(1, "");
            PoiMapCell tmpPoiMapCell1 = new PoiMapCell();
            tmpFloorInfo1.setFloorNo(1);
            tmpPoiMapCell1.setX(mLastPoint.getX());
            tmpPoiMapCell1.setY(mLastPoint.getY());
            tmpPoiMapCell1.setFloorNo(1);
            tmpPoiInfo_from.cell = tmpPoiMapCell1;
            tmpPoiInfo_from.floor = tmpFloorInfo1;
        }else{
            //PoiInfo tmpPoiInfo1 = new PoiInfo();
            tmpPoiInfo_from.PoiInfoType = Constant.TYPE_ROUTE_PLANNING_POI;
            String curSelectId1 = "ZH0000300210100152";
            int floorNo1 = 1;
            IMFloorInfo tmpFloorInfo1 = new IMFloorInfo(floorNo1, "");
            PoiMapCell tmpPoiMapCell1 = new PoiMapCell();
            tmpPoiMapCell1.setFloorNo(floorNo1);
            tmpPoiMapCell1.setPoiId(curSelectId1);
            tmpPoiMapCell1.setName(curSelectId1);
            tmpPoiInfo_from.cell = tmpPoiMapCell1;
            tmpPoiInfo_from.floor = tmpFloorInfo1;
        }
        mInfoFrom = tmpPoiInfo_from;
    }

    /**
     * 路径规划
     */
    public void planningRoad(){
        setBegin();
        if(mInfoFrom==null){
            Toast.makeText(mContext, "请选择起始点", Toast.LENGTH_LONG).show();
            return;
        }
        if(mInfoTo==null){
            Toast.makeText(mContext, "请选择终点", Toast.LENGTH_LONG).show();
            return;
        }

        IMRoutePlanning routePlanning = new IMRoutePlanning(mContext,
                mRoutePlanningListener);
        String buildingId = IMDataManager.getInstance().getCurrentBuildingId();

        PoiMapCell fromMapCell = mInfoFrom.cell;
        PoiMapCell toMapCell = mInfoTo.cell;

        IMLog.logd("####### ------ from PoiInfoType:" + mInfoFrom.PoiInfoType);
        IMLog.logd("####### ------ to PoiInfoType:" + mInfoTo.PoiInfoType);

        if (mInfoFrom.PoiInfoType == Constant.TYPE_ROUTE_PLANNING_LOCATION) {
            routePlanning.excutePlanningPointToPoi(buildingId, fromMapCell.getFloorNo(),
                    fromMapCell.getX(), fromMapCell.getY(), toMapCell.getPoiId());
            IMLog.logd("####### ------ start Point2Poi");
            return;
        }

        if (mInfoTo.PoiInfoType == Constant.TYPE_ROUTE_PLANNING_LOCATION) {

            routePlanning.excutePlanningPoiToPoint(buildingId, fromMapCell.getPoiId(),
                    toMapCell.getFloorNo(), toMapCell.getX(), toMapCell.getY());
            IMLog.logd("####### ------ start Poi2Point");
            return;
        }

        if (mInfoTo.PoiInfoType == Constant.TYPE_ROUTE_PLANNING_POI &&
                mInfoFrom.PoiInfoType == Constant.TYPE_ROUTE_PLANNING_POI) {
            routePlanning.excutePlanningPoiToPoi(buildingId, fromMapCell.getPoiId(),
                    toMapCell.getPoiId());
            IMLog.logd("####### ------ start Poi2Poi");
        }



    }


    /**
     * 路算回调接口（路径规划planningRoad（）回调）
     */
    private IMRoutePlanningListener mRoutePlanningListener = new IMRoutePlanningListener() {
        @Override
        public void onPlanningSuccess(String routePlanningData) {
            // TODO Auto-generated method stub
            IMLog.logd("#######-------- planning success id:" + Thread.currentThread().getId());
            //Toast.makeText(mContext, "路算成功", Toast.LENGTH_LONG).show();
            mIndoorMapFragment.clearRouteStart();
            mIndoorMapFragment.clearRouteStop();
            mIndoorMapFragment.clearSelected();
            mIndoorMapFragment.clearFeatureColor("");
            Gson gson = new Gson();
            PathInfo object= gson.fromJson(routePlanningData, PathInfo.class);
            geometry = object.getRoute().get(0).getPath().getNaviInfoList().get(0).getGeometry();
            //requestCardID(object);
            requestPath(object);   //获取路径成功后向服务器请求数据
            IMLog.logd("#######--------currentroutejson data:" + IMDataManager.getInstance().getRouteData());
            //finish(null);

        }

        @Override
        public void onPlanningFailure(RoutePLanningStatus statusCode) {
            // TODO Auto-generated method stub
            Toast.makeText(mContext, "路算失败,失败码:" + statusCode, Toast.LENGTH_LONG).show();
            IMLog.logd("#######-------- planning failure errorCode:" + statusCode + ", id:" +
                    Thread.currentThread().getId());
        }
    };

    /**
     * 设置路径规划开始点和停止点
     */
    public void drawRouteStartAndStop(String routePlanningData) {
        String fromPoiId = mInfoFrom.cell.getPoiId();
        String toPoiId = mInfoTo.cell.getPoiId();
        IMLog.logd("####### ------ from:" + fromPoiId + ", to:" + toPoiId);

        if (fromPoiId != null && !fromPoiId.equals("")) {
            mIndoorMapFragment.setRouteStart(fromPoiId);
        }

        if (toPoiId != null && !toPoiId.equals("")) {
            mIndoorMapFragment.setRouteStop(toPoiId);
        }

        mIndoorMapFragment.setRouteData(routePlanningData);
        //mIndoorMapFragment.clearRouteResult();
    }

    /**
    * 加载指定的位置
    */
    private PoiInfo loadMylocation() {
        String curBdId = mIndoorMapFragment.getCurrentBuildingId();
        if (mLocationPoint == null || mLocationBdId == null || !mLocationBdId.equals(curBdId)) {
            new AlertDialog.Builder(mIndoorMapFragment.getActivity())
                    .setTitle("提示")
                    .setMessage("没有定位结果,无法使用定位位置!")
                    .setIcon(
                            android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("确定", null).show();
            return null;
        }

        PoiInfo info = new PoiInfo();
        info.PoiInfoType = Constant.TYPE_ROUTE_PLANNING_LOCATION;
        String namecode="F1";

        info.cell = new PoiMapCell(0 ,mLocationPoint.getX(), mLocationPoint.getY(),
                mLocationPoint.getZ(), "我的位置");
        info.floor = new IMFloorInfo(mLocationPoint.getZ(), namecode, "-1");
        return info;
    }

    /**
     * 请求path及Road数据
     * @param pathInfo
     */
    private void requestPath(final PathInfo pathInfo) {
        StringBuilder url = new StringBuilder();
        url.append(Constant.BASE_URL);
        url.append("action=get_walk_by_gda&signKey=abc&building=1");
        url.append("&lng="+geometry.get(0).getX());
        url.append("&lat="+geometry.get(0).getY());
        url.append("&card_id=70");
        String path ;
        Gson gson = new Gson();
        path = gson.toJson(geometry).toString();
        url.append("&path="+path);
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url.toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            Gson gson = new Gson();
                            RoadPathInfo roadInfo = new RoadPathInfo();
                            response = response.getJSONObject("data");
                            roadInfo = gson.fromJson(response.toString(), RoadPathInfo.class);
                            geometry = roadInfo.getPath();
                            road = roadInfo.getRoad();
                            pathInfo.getRoute().get(0).getPath().getNaviInfoList().get(0).setGeometry(geometry);
                            drawRouteStartAndStop(gson.toJson(pathInfo).toString());
                            mIndoorMapFragment.refreshMap();
                            //navigation_ARroad();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonRequest);
    }
}
