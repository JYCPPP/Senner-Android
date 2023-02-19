package com.example.senner.Helper;

import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;

import androidx.annotation.NonNull;

public class LocationHelper implements LocationListener{

    //使用Handler监听位置信息,可监听子线程返回的消息
    private Handler handler = new Handler(msg -> {
        if ( msg.what == 0x001 ) {

        }

        return false;
    });


    @Override
    public void onLocationChanged(@NonNull Location location) {
        // 当GPS定位信息发生改变时，更新定位
        updateLocation(location);
    }

    private void updateLocation(Location location) {

    }
}
