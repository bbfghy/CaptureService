package com.hzj.captureservice.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hzj.captureservice.util.Constant;
import com.hzj.captureservice.util.LogUtil;


public class ScreenCaptureActivity extends Activity {

    private String TAG=ScreenCaptureActivity.class.getSimpleName();


    @Override protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (requestCapturePermission() != 0)
        {
            LogUtil.e(TAG,"android 5.0以下不能通过MediaProjection屏幕截图");
            finish();
        }
    }

    private int requestCapturePermission()
    {
        int rstCode = 0;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            rstCode = 1;
        }
        else
        {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    Constant.REQUEST_MEDIA_PROJECTION);
        }


        return rstCode;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK )
        {
            data.setAction(Constant.BROADCAST_ACTION_SCREEN_CAPTURE);
            sendBroadcast(data);
        }
        else
        {
            LogUtil.e(TAG,"REQUEST_MEDIA_PROJECTION failed!");
        }
        finish();
    }
}
