package com.example.senner.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.senner.Fragment.StartFragment;
import com.example.senner.Helper.PermissionHelper;
import com.example.senner.R;
import com.gyf.immersionbar.ImmersionBar;

public class StartActivity extends AppCompatActivity {

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    public StartActivity() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //检查权限
        PermissionHelper permissionHelper = new PermissionHelper();
        permissionHelper.checkPermissions(this);

        //设置沉浸式状态栏
        ImmersionBar
                .with(this)
                .init();

        BindFragment();
    }

    private void BindFragment() {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StartFragment startFragment = new StartFragment();
        fragmentTransaction
                .replace(R.id.fragment_container_view_tag, startFragment, "Entry")
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
