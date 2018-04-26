
package com.hzj.captureservice.util;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by hzj
 * SharedPreferences 工具类
 */
@SuppressWarnings("unchecked")
public class ShareUtils {

    /**
     * 保存SharedPreferences避免重复实例化
     */
    private static SharedPreferences mSp;
    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "CaptureServer";

    /**
     * 实例化SharedPreferences.Editor
     */
    private static SharedPreferences getSpfs(Context ctx)
    {
        if (mSp == null)
        {
            mSp = ctx.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }
        return mSp;
    }

    /**
     * 保存数据的方法(所有对象以字符串的形式保存)
     *
     * @param ctx
     * @param key
     * @param object
     */
    public static void setParam(Context ctx, String key, Object object)
    {
        SharedPreferences.Editor editor = getSpfs(ctx).edit();
        editor.putString(key, "" + object);
        editor.commit();
    }

    /**
     * 得到保存数据的方法，根据默认值得到需要返回的数据类型，然后将字符串值强转成对应的数据类型
     *
     * @param ctx
     * @param key
     * @param defValue
     * @return
     */
    public static <T> T getParam(Context ctx, String key, T defValue)
    {
        String v = getSpfs(ctx).getString(key, defValue + "");
        T ret = null;
        Class<?> clazz = String.class;// 默认是String类型
        if (defValue != null)
        {
            clazz = defValue.getClass();
        }
        if (defValue instanceof String)
        {
            ret = (T) clazz.cast(v);
        }
        else if (defValue instanceof Boolean)
        {
            ret = (T) clazz.cast(Boolean.parseBoolean(v));
        }
        else if (defValue instanceof Integer)
        {
            ret = (T) clazz.cast(Integer.parseInt(v));
        }
        else if (defValue instanceof Double)
        {
            ret = (T) clazz.cast(Double.parseDouble(v));
        }
        else if (defValue instanceof Float)
        {
            ret = (T) clazz.cast(Float.parseFloat(v));
        }
        else if (defValue instanceof Long)
        {
            ret = (T) clazz.cast(Long.parseLong(v));
        }
        if (null != ret)
        {
            return ret;
        }
        return defValue;
    }





}
