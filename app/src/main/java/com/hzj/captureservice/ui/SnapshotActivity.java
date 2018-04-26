
package com.hzj.captureservice.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hzj.captureservice.R;
import com.hzj.captureservice.service.UdpSnapshotService;


public class SnapshotActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //绑定接收截图服务
        Intent snapshotIntent = new Intent(this, UdpSnapshotService.class);
        startService(snapshotIntent);
    }
}
