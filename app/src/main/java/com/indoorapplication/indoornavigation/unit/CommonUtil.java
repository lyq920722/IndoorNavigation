package com.indoorapplication.indoornavigation.unit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import com.indoorapplication.indoornavigation.config.Constant;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 * 工具类
 * 
 * @author liangyanbin
 * 
 */
public class CommonUtil {

	// 上一次的联网方式
	public static String preNetApn;
	public static boolean isUpdate = false;

	/**
	 * 根据用户的数据类型打开相应的Activity。比如 tel:13400010001打开拨号程序，http://www.g.cn则会打开浏览器等。
	 * 
	 * @param activity
	 * @param url
	 */
	public static void openURL(Activity activity, String url) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(url);
		intent.setData(content_url);
		activity.startActivity(intent);
	}

	/**
	 * 调用系统中的打电话功能 (需要添加打电话权限：<uses-permission
	 * android:name="android.permission.CALL_PHONE" />)
	 * 
	 * @param activity
	 * @param phoneNumber
	 *            你要拨打的电话号码
	 */
	public static void call(Activity activity, String phoneNumber) {
		if (activity != null && phoneNumber != null && !phoneNumber.equals("")) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.CALL");
			intent.setData(Uri.parse("tel:" + phoneNumber));
			activity.startActivity(intent);
		}
	}

	/**
	 * 调用系统中的打电话功能
	 * 
	 * @param context
	 * @param phoneNumber
	 *            你要拨打的电话号码
	 */
	public static void call(Context context, String phoneNumber) {
		if (context != null && phoneNumber != null && !phoneNumber.equals("")) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.CALL");
			intent.setData(Uri.parse("tel:" + phoneNumber));
			context.startActivity(intent);
		}
	}

	// 转码
	public static String toUrlEncode(String str) {
		try {
			if (str == null)
				return null;
			return URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	// 解码
	public static String formUrlEncode(String urlEncodeStr) {
		try {
			if (urlEncodeStr == null)
				return null;
			return URLDecoder.decode(urlEncodeStr, "UTF-8");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * 加载本地图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			java.io.FileInputStream fis = new java.io.FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}





	
	/**
	 * 自定义toast
	 * 
	 * @param context
	 * @param showText
	 * @param isLong
	 */
	/*public static void showToast(Context context, Toast toast, String showText,
			boolean isLong) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.toast, null);
		TextView text = (TextView) layout.findViewById(R.id.toastText);
		text.setText(showText);
		if(toast == null){
			toast = new Toast(context);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		if (isLong) {
			toast.setDuration(Toast.LENGTH_LONG);
		}
		toast.setView(layout);
		toast.show();
	}*/

	/**
	 * 网络是否是打开的(WIFI\cmwap\cmnet)
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkOpen(Context context) {
		boolean isOpen = false;
		try {
			ConnectivityManager cwjManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			isOpen = cwjManager.getActiveNetworkInfo().isAvailable();
		} catch (Exception ex) {
			Log.d("isNetWorkOpen", ex.toString());
			// 如果出异常，那么就是电信3G卡
			isOpen = false;
		}

		return isOpen;
	}



	// 地球半径（米）
	private static final Integer Radius = 6370856;

	/**
	 * 用于计算两个点之间的距离公式
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static float getDistance(float lat1, float lng1, float lat2,
			float lng2) {
		float x, y, distance = 0;
		try {
			x = (float) ((lng2 - lng1) * Math.PI * Radius
					* Math.cos(((lat1 + lat2) / 2) * Math.PI / 180) / 180);
			y = (float) ((lat2 - lat1) * Math.PI * Radius / 180);
			distance = (float) Math.hypot(x, y);
		} catch (Exception e) {

		}
		return distance;
	}
	
	
	

	    private static final double EARTH_RADIUS = 6378137;
		private static final String TAG = "CommonUtil";
	    private static double rad(double d)
	    {
	       return d * Math.PI / 180.0;
	    }
	    
	    /**
	     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
	     * @param lng1
	     * @param lat1
	     * @param lng2
	     * @param lat2
	     * @return
	     */
	    public static double GetDistance(String lng1_str, String lat1_str, String lng2_str, String lat2_str)
	    {
	       double lat1 = Double.parseDouble(lat1_str);
	       double lat2 = Double.parseDouble(lat2_str);
	       double lng1 = Double.parseDouble(lng1_str);
	       double lng2 = Double.parseDouble(lng2_str);
	       double radLat1 = rad(lat1);
	       double radLat2 = rad(lat2);
	       double a = radLat1 - radLat2;
	       double b = rad(lng1) - rad(lng2);
	       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 
	        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	       s = s * EARTH_RADIUS;
	       s = Math.round(s * 10000) / 10000;
	       return s;
	    }


	/**
	 * 用于计算两个点之间的距离公式
	 * 
	 * @param strLat1
	 * @param strLng1
	 * @param strLat2
	 * @param strLng2
	 * @return
	 */
	public static int getDistance(String strLat1, String strLng1,
			String strLat2, String strLng2) {
		float distance = 0;
		try {
			float lat1 = Float.parseFloat(strLat1);
			float lng1 = Float.parseFloat(strLng1);
			float lat2 = Float.parseFloat(strLat2);
			float lng2 = Float.parseFloat(strLng2);
			float x, y;
			x = (float) ((lng2 - lng1) * Math.PI * Radius
					* Math.cos(((lat1 + lat2) / 2) * Math.PI / 180) / 180);
			y = (float) ((lat2 - lat1) * Math.PI * Radius / 180);
			distance = (float) Math.hypot(x, y);
		} catch (Exception e) {

		}
		return (int) distance;
	}

	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	public static Bitmap bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}
	
	public static String bitmap2Base64String(Bitmap bm){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 30, bos);// 参数100表示不压缩
		byte[] bytes = bos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
//		return Base64.encodeToString(bitmap2Bytes(bm), Base64.DEFAULT);
	}

	public static Bitmap base64StringToBitmap(String base64String) {
		// 将字符串转换成Bitmap类型
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(base64String, Base64.DEFAULT);
			bitmap = bytes2Bimap(bitmapArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	// *********************图片尺寸
	// n0: 608x608
	// n1: 350x350
	// n2： 160x160
	// n3: 130x130
	// n4: 100x100
	// n5: 50x50
	// 图片前缀。用于订单列表跟pic字段拼接 (如需要不同的尺寸,替换n2)
	public static String PIC_PREFIX = "http://img10.360buyimg.com/n2";
	
	//测试数据开关
//	public static boolean isUseTest = false;
	
	public static void initDisplayMetrics(Activity context) {
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
	    int width = metric.widthPixels;  	// 屏幕宽度（像素）
	    int height = metric.heightPixels;  	// 屏幕高度（像素）
	    float density = metric.density;  	// 屏幕密度（0.75 / 1.0 / 1.5 /2.0）
		Constant.height = height;
		Constant.width = width;
		Constant.density = density;
	}
	public static long getTimestamp() {
        long timestamp = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        Date date = new Date(timestamp);
//        String strDate = sdf.format(date);
        return timestamp;
    }
	/**
	 * 格式化两位小数
	 * @param num
	 * @return
	 */
	public static String formatData(double num) {
		DecimalFormat    df   = new DecimalFormat("######0.00");   
		String numstr = df.format(num);
		return numstr;
	}
	
	public static float getMargin(float length){
		float mylength = length;
		if(Constant.density!=0){
			if(Constant.density==1){
				mylength = length + 5;
			}else{
				mylength = length*Constant.density;
			}
		}
		return mylength;
	}
	public static float getWidth(float length){
		float mylength = length;
		if(Constant.density!=0){
			if(Constant.density==1){
				mylength = length + 10;
			}else{
				mylength = length*Constant.density;
			}
		}
		return mylength;
	}
	/**
	 * TextView设置字体大小时使用
	 * @param textsize
	 * @return
	 */
	public static float getTextSize(float textsize){
		float mytextsize= textsize;
		if(Constant.density!=0){
			if(Constant.density==1){
				mytextsize = textsize + 3;
			}else{
				mytextsize = textsize;
			}
		}
		return mytextsize;
	}
	
	public static float getTextSizeByDensity(float textsize){
		float mytextsize= textsize;
		if(Constant.density!=0){
			if(Constant.density==1){
				mytextsize = textsize + 10;
			}else{
				mytextsize = textsize*Constant.density;
			}
		}
		return mytextsize;
	}
	
	public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

//	/**
//	 * DES算法，加密
//	 * 
//	 * @param data
//	 *            待加密字符串
//	 * @param key
//	 *            加密私钥，长度不能够小于8位
//	 * @return 加密后的字节数组，一般结合Base64编码使用
//	 * @throws InvalidAlgorithmParameterException
//	 * @throws Exception
//	 */
//	public static String encode(String data) {
//		String key = "~_~smart^^";
//		
//		if (data == null)
//			return null;
//		try {
//			DESKeySpec dks = new DESKeySpec(key.getBytes());
//			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
//			// key的长度不能够小于8位字节
//			Key secretKey = keyFactory.generateSecret(dks);
//			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
//			IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
//			AlgorithmParameterSpec paramSpec = iv;
//			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
//			byte[] bytes = cipher.doFinal(data.getBytes());
//			return byte2String(bytes);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return data;
//		}
//	}
	/**
	 * 加密
	 * @param strIn
	 * @return
	 */
	public static String encodeDes(String strIn) {
        try {
            Cipher e = getDesCipher("encrypt");
            return byteArr2HexStr(e.doFinal(strIn.getBytes()));
        } catch (Exception var2) {
            return null;
        }
    }
	private static Cipher getDesCipher(String type) {
        Cipher cipher = null;

        try {
            DESKeySpec e = new DESKeySpec("~_~smart^^".getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(e);
            cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            if("encrypt".equals(type)) {
                cipher.init(1, secretKey);
            } else {
                cipher.init(2, secretKey);
            }
        } catch (Exception var5) {
        }

        return cipher;
    }
    public static String byteArr2HexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;
        StringBuffer sb = new StringBuffer(iLen * 2);

        for(int i = 0; i < iLen; ++i) {
            int intTmp;
            for(intTmp = arrB[i]; intTmp < 0; intTmp += 256) {
                ;
            }
            if(intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16).toUpperCase());
        }
        return sb.toString();
    }
    /**
     * 解密
     * @param strIn
     * @return
     */
    public static String decodeDes(String strIn) {
        try {
            Cipher e = getDesCipher("decode");
            return new String(e.doFinal(hexStr2ByteArr(strIn)));
        } catch (Exception var2) {
            return null;
        }
    }
    private static byte[] hexStr2ByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;
        byte[] arrOut = new byte[iLen / 2];

        for(int i = 0; i < iLen; i += 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte)Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }
//	/**
//	 * DES算法，解密
//	 * 
//	 * @param data
//	 *            待解密字符串
//	 * @param key
//	 *            解密私钥，长度不能够小于8位
//	 * @return 解密后的字节数组
//	 * @throws Exception
//	 *             异常
//	 */
//	public static String decode( String data) {
//		String key = "~_~smart^^";
//		if (data == null)
//			return null;
//		try {
//			DESKeySpec dks = new DESKeySpec(key.getBytes());
//			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
//			// key的长度不能够小于8位字节
//			Key secretKey = keyFactory.generateSecret(dks);
//			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
//			IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
//			AlgorithmParameterSpec paramSpec = iv;
//			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
//			return new String(cipher.doFinal(byte2hex(data.getBytes())));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return data;
//		}
//	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @return
	 */
	private static String byte2String(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString().toUpperCase(Locale.CHINA);
	}

	/**
	 * 二进制转化成16进制
	 * 
	 * @param b
	 * @return
	 */
	private static byte[] byte2hex(byte[] b) {
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException();
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}
	

	
}
