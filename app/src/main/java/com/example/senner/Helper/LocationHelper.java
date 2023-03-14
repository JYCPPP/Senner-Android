package com.example.senner.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class LocationHelper {
    private Activity activity;
    private LocationManager manager;

    LocationHelper(Activity activity){
        this.activity = activity;
    }

    /**
     * 初始化定位管理,android自带卫星
     */
    private void initLocation() {
        manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        //判断GPS是否正常启动
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
            return;
        }
        //添加卫星状态改变监听
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.registerGnssStatusCallback(new LocaCallback());
        //1000位最小的时间间隔，1为最小位移变化；也就是说每隔1000ms会回调一次位置信息
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                // 在这里处理经纬度
            }

            @Override
            public void onLocationChanged(@NonNull List<Location> locations) {
                LocationListener.super.onLocationChanged(locations);
            }

            @Override
            public void onFlushComplete(int requestCode) {
                LocationListener.super.onFlushComplete(requestCode);
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    public static class LocaCallback extends GnssStatus.Callback {
        int satelliteCount;
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            super.onSatelliteStatusChanged(status);
            satelliteCount = status.getSatelliteCount();
            //解析组装卫星信息
            makeGnssStatus(status, satelliteCount);
        }

        @Override
        public void onStarted() {
            super.onStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }

        private void makeGnssStatus(GnssStatus status, int satelliteCount){
            //当前可以获取到的卫星总数，然后遍历
            if(satelliteCount>0){
                for (int i = 0; i < satelliteCount; i++) {
                    //GnssStatus的大部分方法参数传入的就是卫星数量的角标
                    //获取卫星类型
                    int type=status.getConstellationType(i);
                    if(GnssStatus.CONSTELLATION_BEIDOU==type){
                        // 北斗卫星类型的判断,如果是北斗卫星
                        //

                    }
                }
            }
        }
    }

}
