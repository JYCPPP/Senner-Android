package com.example.senner.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.senner.Fragment.LoginFragment;
import com.example.senner.Helper.RandomHelper;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.R;
import com.example.senner.UI.CustomVideoView;
import com.gyf.immersionbar.ImmersionBar;


public class LoginActivity extends AppCompatActivity {

    private CustomVideoView customVideoView;
    //要使用的背景视频
    private final int[] assetlist = new int[]{R.raw.bg_login1, R.raw.bg_login2, R.raw.bg_login3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //设置沉浸式状态栏
        ImmersionBar
                .with(this)
                .init();


        // session manager
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();

        // check user is already logged in
        if (sharedPreferenceHelper.getBoolean(this, "isloggedin", true)) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
        BindFragment();

        //设置背景视频
        SetVieoView();


    }

    private void BindFragment() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        fragmentTransaction
                .setCustomAnimations(R.anim.fragment_flip_in, R.anim.fragment_flip_out)
                .replace(R.id.fcv_login, loginFragment, "Entry")
                .commit();
    }

    private void SetVieoView() {

        customVideoView = findViewById(R.id.vv_loginbg);
        Thread thread = new Thread(() -> {


            String path = "android.resource://" + getPackageName() +"/" + assetlist[RandomHelper.getNum(0, assetlist.length)];
            Uri uri = Uri.parse(path);
            customVideoView.setVideoURI(uri);
            customVideoView.setOnPreparedListener(mp -> {
                // 通过MediaPlayer设置循环播放
                mp.setLooping(true);
                // OnPreparedListener中的onPrepared方法是在播放源准备完成后回调的，所以可以在这里开启播放
                mp.start();
            });
        });
        thread.start();
    }
}