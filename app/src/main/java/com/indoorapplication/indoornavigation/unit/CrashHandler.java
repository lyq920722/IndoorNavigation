package com.indoorapplication.indoornavigation.unit;

/**
 * Created by liyuanqing on 2016/10/23.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

public class CrashHandler  implements UncaughtExceptionHandler {

    private static CrashHandler crashHandler;

    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // TODO Auto-generated method stub
        if (crashHandler != null) {
            try {
                //将crash log写入文件
                FileOutputStream fileOutputStream = new FileOutputStream("/mnt/sdcard/crash_log.txt", true);
                PrintStream printStream = new PrintStream(fileOutputStream);
                ex.printStackTrace(printStream);
                printStream.flush();
                printStream.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //设置默认处理器
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private CrashHandler() {
    }

    //单例
    public static CrashHandler instance() {
        if (crashHandler == null) {
            //synchronized (crashHandler) {
                crashHandler = new CrashHandler();
            //}
        }
        return crashHandler;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("crash", "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d("crash", field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e("crash", "an error occured when collect crash info", e);
            }
        }
    }

}