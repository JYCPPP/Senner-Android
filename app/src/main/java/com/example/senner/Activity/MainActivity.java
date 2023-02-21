package com.example.senner.Activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.senner.Fragment.ObjectDetectionFragment;
import com.example.senner.Fragment.ProjectFragment;
import com.example.senner.Fragment.SensorsFragment;
import com.example.senner.Fragment.UserFragment;
import com.example.senner.R;
import com.example.senner.UI.ViewPageAdapter;
import com.github.mikephil.charting.renderer.RadarChartRenderer;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gyf.immersionbar.ImmersionBar;

import java.lang.reflect.Field;
import java.util.ArrayList;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ArrayList<Fragment> fragments;
    private ArrayList<Drawable> icons;
    private ViewPageAdapter viewPageAdapter;
    private BlurView blurView_bottom;
    private ViewGroup root;

    private EditText ProjectName;
    private CheckBox UseLinearAcc, NeedLinearAccThresh,
                    UseAcc, NeedAccThresh,
                    UseGyro, NeedGyroThresh,
                    UseRot, NeedRotThresh,
                    UseMRot, NeedMRotThresh,
                    UseMag, NeedMagThresh,
                    UseProximity, NeedProximityThresh,
                    UseLight, NeedLightThresh,
                    UseTemp, NeedTempThresh,
                    UsePressure, NeedPressureThresh,
                    UseHumidity, NeedHumidityThresh,
                    UseStep, NeedStepThresh,
                    UseObjectDetection;

    private EditText LinearAccThreshX, LinearAccThreshY, LinearAccThreshZ,
                    AccThreshX, AccThreshY, AccThreshZ,
                    GyroThreshX, GyroThreshY, GyroThreshZ,
                    RotThreshX, RotThreshY, RotThreshZ,
                    MRotThreshX, MRotThreshY, MRotThreshZ,
                    MagThreshX, MagThreshY, MagThreshZ,
                    ProximityThresh, LightThresh, TempThresh,
                    PressureThresh, HumidityThresh, StepThresh;

    private Button StartButton;

    private LinearLayout LinearAccNeedThreshMenu, LinearAccSetThreshMenu,
                        AccNeedThreshMenu, AccSetThreshMenu,
                        GyroNeedThreshMenu, GyroSetThreshMenu,
                        RotNeedThreshMenu, RotSetThreshMenu,
                        MRotNeedThreshMenu, MRotSetThreshMenu,
                        MagNeedThreshMenu, MagSetThreshMenu,
                        ProximityNeedThreshMenu, ProximitySetThreshMenu,
                        LightNeedThreshMenu, LightSetThreshMenu,
                        PressureNeedThreshMenu, PressureSetThreshMenu,
                        TempNeedThreshMenu, TempSetThreshMenu,
                        StepNeedThreshMenu, StepSetThreshMenu,
                        HumidityNeedThreshMenu, HumiditySetThreshMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置沉浸式状态栏
        ImmersionBar.with(this).init();
        Init();
        SetCheckBox();
        SetViewPage();
        reviseViewpagerConfigurePara();
    }



    /**
     * 初始化UI控件
     */
    @SuppressLint("SetTextI18n")
    private void Init(){

        //绑定控件
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        LinearAccNeedThreshMenu = findViewById(R.id.option_linearacc_need_Thresh);
        LinearAccSetThreshMenu = findViewById(R.id.option_linearacc_Thresh);
        AccNeedThreshMenu = findViewById(R.id.option_acc_need_Thresh);
        AccSetThreshMenu = findViewById(R.id.option_acc_Thresh);
        GyroNeedThreshMenu = findViewById(R.id.option_gyro_need_Thresh);
        GyroSetThreshMenu = findViewById(R.id.option_gyro_Thresh);
        RotNeedThreshMenu = findViewById(R.id.option_rot_need_Thresh);
        RotSetThreshMenu = findViewById(R.id.option_rot_Thresh);
        MRotNeedThreshMenu = findViewById(R.id.option_mrot_need_Thresh);
        MRotSetThreshMenu = findViewById(R.id.option_mrot_Thresh);
        MagNeedThreshMenu = findViewById(R.id.option_mag_need_Thresh);
        MagSetThreshMenu = findViewById(R.id.option_mag_Thresh);
        ProximityNeedThreshMenu = findViewById(R.id.option_proximity_need_Thresh);
        ProximitySetThreshMenu = findViewById(R.id.option_proximity_Thresh);
        LightNeedThreshMenu = findViewById(R.id.option_light_need_Thresh);
        LightSetThreshMenu = findViewById(R.id.option_light_Thresh);
        PressureNeedThreshMenu = findViewById(R.id.option_pressure_need_Thresh);
        PressureSetThreshMenu = findViewById(R.id.option_pressure_Thresh);
        TempNeedThreshMenu = findViewById(R.id.option_temp_need_Thresh);
        TempSetThreshMenu = findViewById(R.id.option_temp_Thresh);
        StepNeedThreshMenu = findViewById(R.id.option_step_need_Thresh);
        StepSetThreshMenu = findViewById(R.id.option_step_Thresh);
        HumidityNeedThreshMenu = findViewById(R.id.option_humidity_need_Thresh);
        HumiditySetThreshMenu = findViewById(R.id.option_humidity_Thresh);


        //绑定CheckBox
        ProjectName = findViewById(R.id.et_projectName);

        UseLinearAcc = findViewById(R.id.cb_linearacc);
        NeedLinearAccThresh = findViewById(R.id.cb_linearacc_need_thresh);
        UseAcc = findViewById(R.id.cb_acc);
        NeedAccThresh = findViewById(R.id.cb_acc_need_thresh);
        UseGyro = findViewById(R.id.cb_gyro);
        NeedGyroThresh = findViewById(R.id.cb_gyro_need_thresh);
        UseRot = findViewById(R.id.cb_rot);
        NeedRotThresh = findViewById(R.id.cb_rot_need_thresh);
        UseMRot = findViewById(R.id.cb_mrot);
        NeedMRotThresh = findViewById(R.id.cb_mrot_need_thresh);
        UseMag = findViewById(R.id.cb_mag);
        NeedMagThresh = findViewById(R.id.cb_mag_need_thresh);
        UseProximity = findViewById(R.id.cb_proximity);
        NeedProximityThresh = findViewById(R.id.cb_proximity_need_thresh);
        UseLight = findViewById(R.id.cb_light);
        NeedLightThresh = findViewById(R.id.cb_light_need_thresh);
        UseTemp = findViewById(R.id.cb_temp);
        NeedTempThresh = findViewById(R.id.cb_temp_need_thresh);
        UsePressure = findViewById(R.id.cb_pressure);
        NeedPressureThresh = findViewById(R.id.cb_pressure_need_thresh);
        UseHumidity = findViewById(R.id.cb_humidity);
        NeedHumidityThresh = findViewById(R.id.cb_humidity_need_thresh);
        UseStep = findViewById(R.id.cb_step);
        NeedStepThresh = findViewById(R.id.cb_step_need_thresh);

        UseObjectDetection = findViewById(R.id.cb_object_detection);

        //绑定EditBox
        LinearAccThreshX = findViewById(R.id.et_linearacc_threshX);
        LinearAccThreshY = findViewById(R.id.et_linearacc_threshY);
        LinearAccThreshZ = findViewById(R.id.et_linearacc_threshZ);
        AccThreshX = findViewById(R.id.et_acc_threshX);
        AccThreshY = findViewById(R.id.et_acc_threshY);
        AccThreshZ = findViewById(R.id.et_acc_threshZ);
        GyroThreshX = findViewById(R.id.et_gyro_threshX);
        GyroThreshY = findViewById(R.id.et_gyro_threshY);
        GyroThreshZ = findViewById(R.id.et_gyro_threshZ);
        RotThreshX = findViewById(R.id.et_rot_threshX);
        RotThreshY = findViewById(R.id.et_rot_threshY);
        RotThreshZ = findViewById(R.id.et_rot_threshZ);
        MRotThreshX = findViewById(R.id.et_mrot_threshX);
        MRotThreshY = findViewById(R.id.et_mrot_threshY);
        MRotThreshZ = findViewById(R.id.et_mrot_threshZ);
        MagThreshX = findViewById(R.id.et_mag_threshX);
        MagThreshY = findViewById(R.id.et_mag_threshY);
        MagThreshZ = findViewById(R.id.et_mag_threshZ);
        ProximityThresh = findViewById(R.id.et_proximity_thresh);
        LightThresh = findViewById(R.id.et_light_thresh);
        TempThresh = findViewById(R.id.et_temp_thresh);
        PressureThresh = findViewById(R.id.et_pressure_thresh);
        HumidityThresh = findViewById(R.id.et_humidity_thresh);
        StepThresh = findViewById(R.id.et_step_thresh);

        //绑定按钮
        StartButton = findViewById(R.id.btn_start);

        //设置高斯模糊
        blurView_bottom = findViewById(R.id.blurView_bottom);
        root = findViewById(R.id.root);
        setupBlurView();

    }

    private boolean IsUseObjectDetection = false;
    private void SetCheckBox() {

        UseLinearAcc.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseLinearAcc, LinearAccNeedThreshMenu, NeedLinearAccThresh, LinearAccSetThreshMenu));
        UseAcc.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseAcc, AccNeedThreshMenu, NeedAccThresh, AccSetThreshMenu));
        UseGyro.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseGyro, GyroNeedThreshMenu, NeedGyroThresh, GyroSetThreshMenu));
        UseRot.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseRot, RotNeedThreshMenu, NeedRotThresh, RotSetThreshMenu));
        UseMRot.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseMRot, MRotNeedThreshMenu, NeedMRotThresh, MRotSetThreshMenu));
        UseMag.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseMag, MagNeedThreshMenu, NeedMagThresh, MagSetThreshMenu));
        UseProximity.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseProximity, ProximityNeedThreshMenu, NeedProximityThresh,ProximitySetThreshMenu));
        UseLight.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseLight, LightNeedThreshMenu, NeedLightThresh, LightSetThreshMenu));
        UseTemp.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseTemp, TempNeedThreshMenu, NeedTempThresh, TempSetThreshMenu));
        UsePressure.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UsePressure, PressureNeedThreshMenu, NeedPressureThresh, PressureSetThreshMenu));
        UseHumidity.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseHumidity, HumidityNeedThreshMenu, NeedHumidityThresh, HumiditySetThreshMenu));
        UseStep.setOnCheckedChangeListener((v, isChecked) -> SetNeedThreshMenuVisibility(UseStep, StepNeedThreshMenu, NeedStepThresh, StepSetThreshMenu));
        UseObjectDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {IsUseObjectDetection = UseObjectDetection.isChecked();});
    }

    private void SetNeedThreshMenuVisibility(CheckBox UseSensor, LinearLayout NeedThreshMenu, CheckBox NeedThresh, LinearLayout SetThreshMenu) {
        if(UseSensor.isChecked()){
            NeedThreshMenu.setVisibility(View.VISIBLE);
            NeedThresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(NeedThresh.isChecked()){
                    SetThreshMenu.setVisibility(View.VISIBLE);
                }else{
                    SetThreshMenu.setVisibility(View.GONE);
                }
            });
        }else{
            NeedThresh.setChecked(false);
            NeedThreshMenu.setVisibility(View.GONE);
            SetThreshMenu.setVisibility(View.GONE);
        }

    }

    /**
     * 设置高斯模糊
     */
    private void setupBlurView() {

        final float radius = 25f;

        final Drawable windowBackground = getWindow().getDecorView().getBackground();
        BlurAlgorithm algorithm = getBlurAlgorithm();


        blurView_bottom.setupWith(root, algorithm)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
    }

    /**
     * 根据版本确定所用算法
     * @return 算法类型
     */
    @NonNull
    private BlurAlgorithm getBlurAlgorithm() {
        BlurAlgorithm algorithm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            algorithm = new RenderEffectBlur();
        } else {
            algorithm = new RenderScriptBlur(this);
        }
        return algorithm;
    }

    /**
     * 设置点击drawer的切换动作
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void SetViewPage() {

        //禁用预加载
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        fragments = new ArrayList<>();
        icons = new ArrayList<>();
        fragments.add(new SensorsFragment());
        fragments.add(new ObjectDetectionFragment());
        fragments.add(new ProjectFragment());
        fragments.add(new UserFragment());
        icons.add(getDrawable(R.drawable.round_sensors_36dp));
        icons.add(getDrawable(R.drawable.round_cv_36dp));
        icons.add(getDrawable(R.drawable.round_projects_36dp));
        icons.add(getDrawable(R.drawable.round_user_36dp));

        viewPageAdapter = new ViewPageAdapter(this, fragments);
        viewPager.setAdapter(viewPageAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setIcon(icons.get(position))).attach();
    }

    public void reviseViewpagerConfigurePara() {


        try {
            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);

            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 6);//6 is empirical value
        } catch (Exception ignore) {
        }
    }
}