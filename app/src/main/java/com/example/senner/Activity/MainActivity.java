package com.example.senner.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.senner.Fragment.ObjectDetectionFragment;
import com.example.senner.Fragment.ProjectFragment;
import com.example.senner.Fragment.SensorRecordFragment;
import com.example.senner.Fragment.UserFragment;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.R;
import com.example.senner.UI.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gyf.immersionbar.ImmersionBar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.github.muddz.styleabletoast.StyleableToast;

public class MainActivity extends AppCompatActivity  {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ArrayList<Drawable> icons;
    private BlurView blurView_bottom;
    private ViewGroup root;
    private FrameLayout menu;

    private EditText ProjectName;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch UseLinearAcc, NeedLinearAccthresh,
                    UseAcc, NeedAccthresh,
                    UseGyro, NeedGyrothresh,
                    UseRot, NeedRotthresh,
                    UseMRot, NeedMRotthresh,
                    UseMag, NeedMagthresh,
                    UseProximity, NeedProximitythresh,
                    UseLight, NeedLightthresh,
                    UseTemp, NeedTempthresh,
                    UsePressure, NeedPressurethresh,
                    UseHumidity, NeedHumiditythresh,
                    UseStep, NeedStepthresh,
                    UseObjectDetection;

    private EditText LinearAccthreshX, LinearAccthreshY, LinearAccthreshZ,
                    AccthreshX, AccthreshY, AccthreshZ,
                    GyrothreshX, GyrothreshY, GyrothreshZ,
                    RotthreshX, RotthreshY, RotthreshZ,
                    MRotthreshX, MRotthreshY, MRotthreshZ,
                    MagthreshX, MagthreshY, MagthreshZ,
                    Proximitythresh, Lightthresh, Tempthresh,
                    Pressurethresh, Humiditythresh, Stepthresh;


    private Button StartButton;

    private LinearLayout LinearAccNeedthreshMenu, LinearAccSetthreshMenu,
                        AccNeedthreshMenu, AccSetthreshMenu,
                        GyroNeedthreshMenu, GyroSetthreshMenu,
                        RotNeedthreshMenu, RotSetthreshMenu,
                        MRotNeedthreshMenu, MRotSetthreshMenu,
                        MagNeedthreshMenu, MagSetthreshMenu,
                        ProximityNeedthreshMenu, ProximitySetthreshMenu,
                        LightNeedthreshMenu, LightSetthreshMenu,
                        PressureNeedthreshMenu, PressureSetthreshMenu,
                        TempNeedthreshMenu, TempSetthreshMenu,
                        StepNeedthreshMenu, StepSetthreshMenu,
                        HumidityNeedthreshMenu, HumiditySetthreshMenu;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置沉浸式状态栏
        ImmersionBar.with(this).init();
        //绑定控件
        Init();
        //设置CheckBox，保存勾选状态
        SetCheckBox();
        //设置点击开始按钮要做的事情
        SetStartButton();
        //设置输入状态的转换
        SetEditTextFoucus();
        //设置进入动画
        CloseViewPage();

    }

    /**
     * 处理输入框的聚焦逻辑，避免一直在输入状态
     */
    private void SetEditTextFoucus() {

        List<EditText> editTexts = List.of(
                LinearAccthreshX, LinearAccthreshY, LinearAccthreshZ,
                AccthreshX, AccthreshY, AccthreshZ,
                GyrothreshX, GyrothreshY, GyrothreshZ,
                RotthreshX, RotthreshY, RotthreshZ,
                MRotthreshX, MRotthreshY, MRotthreshZ,
                MagthreshX, MagthreshY, MagthreshZ,
                Proximitythresh, Lightthresh, Tempthresh,
                Pressurethresh, Humiditythresh, Stepthresh);

        for(EditText editText : editTexts){

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    // 输入完成后的逻辑处理，例如关闭输入法等
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            });
        }
    }

    //项目综述
    private String ProjectInfo = "";
    private final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();
    private boolean UseSensor;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("UseCompatLoadingForDrawables")
    private void SetStartButton() {
        //点击开始按钮应该依次执行以下事件
        //确保项目名称输入框非空，并创建相应名称文件夹
        //先判断使用阈值与输入阈值的冲突关系
        StartButton.setOnClickListener(v -> {

            if(ProjectName.getText().toString().isEmpty()){
                //触发震动事件
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                VibrationEffect effect = VibrationEffect.createOneShot(200, 200);
                // 检查设备是否支持 VibratorEffect 对象
                if (vibrator.hasAmplitudeControl()) {
                    // 设备支持 VibratorEffect 对象
                    vibrator.vibrate(effect);
                } else {
                    // 设备不支持 VibratorEffect 对象
                    vibrator.vibrate(200);
                }

                StyleableToast.makeText(MainActivity.this, "Invalid Project Name", R.style.WarningToast).show();
                ProjectName.setBackground(getDrawable(R.drawable.bg_red));
            }else{

                //时间
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 将时区设置为东八区
                Date currentDate = new Date();
                String currentTime = sdf.format(currentDate.getTime());
                ProjectInfo = "Project Name: " + ProjectName.getText().toString() + "\n" + "Create Time: " + currentTime + "\n";

                //判断Switch所处状态
                CheckSensorOptionState(UseLinearAcc, NeedLinearAccthresh, LinearAccthreshX, LinearAccthreshY, LinearAccthreshZ, "Linear Accelerometer", "m/s^2");
                CheckSensorOptionState(UseAcc, NeedAccthresh, AccthreshX, AccthreshY, AccthreshZ, "Accelerometer", "m/s^2");
                CheckSensorOptionState(UseRot, NeedRotthresh, RotthreshX, RotthreshY, RotthreshZ, "Rotation Vector Sensor", " ");
                CheckSensorOptionState(UseMRot, NeedMRotthresh, MRotthreshX, MRotthreshY, MRotthreshZ, "Geomagnetic Rotation Vector Sensor", " ");
                CheckSensorOptionState(UseMag, NeedMagthresh, MagthreshX, MagthreshY, MagthreshZ, "Magnetic Field Sensor", "μT");
                CheckSensorOptionState(UseGyro, NeedGyrothresh, GyrothreshX, GyrothreshY, GyrothreshZ, "Gyroscope", "°");
                CheckSensorOptionState(UseProximity, NeedProximitythresh, Proximitythresh, "Proximity Sensor", "cm");
                CheckSensorOptionState(UseLight, NeedLightthresh, Lightthresh, "Light Sensor", "lx");
                CheckSensorOptionState(UseTemp, NeedTempthresh, Tempthresh, "Ambient Temperature Sensor", "℃");
                CheckSensorOptionState(UseHumidity, NeedHumiditythresh, Humiditythresh, "Relative Humidity Sensor", "%");
                CheckSensorOptionState(UsePressure, NeedPressurethresh, Pressurethresh, "Pressure Sensor", "hPa");
                CheckSensorOptionState(UseStep, NeedStepthresh, Stepthresh, "Step Counter", "steps");

                //下面进行两件事情
                //一是点击开始按钮加载主界面，主界面的内容与自定义的需求有关
                //确保至少选择一个项目
                //定制主界面
                if(UseSensor || UseObjectDetection.isChecked()){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //已经授予文件读写权限
                        //创建文件夹并保存文件
                        //二是在子线程中进行文件读写
                        new Thread(() -> {
                            // 在子线程中执行文件夹创建和文件读写操作
                            File Project = new File(getApplicationContext().getFilesDir().getAbsolutePath() +  "/Senner/" + ProjectName.getText());
                            sharedPreferenceHelper.putString(this,"APP Path", getApplicationContext().getFilesDir().getAbsolutePath() +  "/Senner");
                            sharedPreferenceHelper.putString(this,"Project Path", Project.getPath() + "/");

                            try {
                                if (Project.exists()) {
                                    Project.delete(); //删除原有文件夹
                                }
                                Project.mkdirs(); //创建新的文件夹
                                //项目综述文件
                                File file = new File(Project.getPath() + "/ProjectInfo.txt");
                                if (file.exists()) {
                                    file.delete(); //删除原有文件夹
                                }
                                file.createNewFile();

                                FileWriter writer = new FileWriter(file);
                                writer.write(ProjectInfo);
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //在子线程中使用Handler更新UI
                            new Handler(Looper.getMainLooper()).post(() -> {
                                //在主线程中更新UI
                                SetViewPage();
                                reviseViewpagerConfigurePara();
                                StyleableToast.makeText(MainActivity.this, "Prepared!", R.style.RightToast).show();
                                OpenViewPage();
                            });
                        }).start();
                    } else {
                        //尚未授予文件读写权限
                        //申请文件读写权限
                        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                    }
                }else{
                    Vibrate(200);
                    StyleableToast.makeText(MainActivity.this, "Choose Nothing!", R.style.WarningToast).show();
                }

            }

        });
    }

    private void OpenViewPage() {

        //设置动画
        Animation MenuAnimation = AnimationUtils.loadAnimation(this, R.anim.main_sheet_out);
        //设置背景隐藏
        menu.setClickable(false);
        menu.setFocusable(false);
        menu.startAnimation(MenuAnimation);
        menu.setVisibility(View.GONE);
        //主界面过渡动画
        Animation ViewPageAnimation = AnimationUtils.loadAnimation(this, R.anim.main_view_in);
        viewPager.setClickable(true);
        viewPager.setFocusable(true);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.startAnimation(ViewPageAnimation);
        //如果主界面动画结束，TabLayout缓缓升起
        Animation TabAnimation = AnimationUtils.loadAnimation(this, R.anim.main_tab_up);
        tabLayout.setClickable(true);
        tabLayout.setFocusable(true);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.startAnimation(TabAnimation);
    }

    private void CloseViewPage(){
        //如果主界面动画结束，TabLayout缓缓升起
        Animation TabAnimation = AnimationUtils.loadAnimation(this, R.anim.main_tab_down);
        tabLayout.setClickable(false);
        tabLayout.setFocusable(false);
        tabLayout.startAnimation(TabAnimation);
        tabLayout.setVisibility(View.GONE);
        //主界面过渡动画
        Animation ViewPageAnimation = AnimationUtils.loadAnimation(this, R.anim.main_view_out);
        viewPager.setClickable(false);
        viewPager.setFocusable(false);
        viewPager.startAnimation(ViewPageAnimation);
        viewPager.setVisibility(View.GONE);
        //设置动画
        Animation MenuAnimation = AnimationUtils.loadAnimation(this, R.anim.main_sheet_in);
        //设置背景隐藏
        menu.setClickable(true);
        menu.setFocusable(true);
        menu.startAnimation(MenuAnimation);
        menu.setVisibility(View.VISIBLE);
    }

    private void Vibrate(int amplify) {
        //触发震动事件
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect effect = VibrationEffect.createOneShot(100, amplify);
        // 检查设备是否支持 VibratorEffect 对象
        if (vibrator.hasAmplitudeControl()) {
            // 设备支持 VibratorEffect 对象
            vibrator.vibrate(effect);
        } else {
            // 设备不支持 VibratorEffect 对象
            vibrator.vibrate(100);
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceType", "SetTextI18n"})
    private void CheckSensorOptionState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch useSensor, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch needthresh, EditText threshX, EditText threshY, EditText threshZ, String SensorName, String Dimension) {

        if(useSensor.isChecked() && needthresh.isChecked() && !threshX.getText().toString().isEmpty() && !threshY.getText().toString().isEmpty() && !threshZ.getText().toString().isEmpty()){

            ProjectInfo += "Use "+ SensorName + " : " + "true" + "\n" + "Need Thresh: " + "true " + "\r" + threshX.getText().toString() +" " + Dimension +", " + threshY.getText().toString() +" " + Dimension  + ", " + threshZ.getText().toString() + " " + Dimension + "\n";
        }
        else if(!useSensor.isChecked()){

            ProjectInfo += "Use "+ SensorName + " : "  + "false" + "\n";
        }
        else if(useSensor.isChecked() && !needthresh.isChecked()){

            ProjectInfo += "Use "+ SensorName + " : "  + "true" + "\n" + "Need Thresh: " + "false" + "\n";
        }
        else if(useSensor.isChecked() && needthresh.isChecked() && (threshX.getText().toString().isEmpty() || threshY.getText().toString().isEmpty() || threshZ.getText().toString().isEmpty())){

            if(threshX.getText().toString().isEmpty()){
                threshX.setText("9999");
                threshX.setBackground(getDrawable(R.drawable.bg_red));
            }
            if(threshY.getText().toString().isEmpty()){
                threshY.setText("9999");
                threshY.setBackground(getDrawable(R.drawable.bg_red));
            }
            if(threshZ.getText().toString().isEmpty()){
                threshZ.setText("9999");
                threshZ.setBackground(getDrawable(R.drawable.bg_red));
            }
            ProjectInfo += "Use "+ SensorName + " : " + "true" + "\n" + "Need Thresh: " + "true " + "\r" + threshX.getText().toString() +" " + Dimension +", " + threshY.getText().toString() +" " + Dimension  + ", " + threshZ.getText().toString() + " " + Dimension + "\n";
        }

        if(useSensor.isChecked()){
            UseSensor = true;
            sharedPreferenceHelper.putBoolean(this, "Use " + SensorName , true);
        }else{
            sharedPreferenceHelper.putBoolean(this, "Use " + SensorName , false);
        }

        if(needthresh.isChecked()){
            sharedPreferenceHelper.putBoolean(this, "Need Thresh " + SensorName , true);
            sharedPreferenceHelper.putFloat(this, "Thresh X " + SensorName, Float.parseFloat(threshX.getText().toString()));
            sharedPreferenceHelper.putFloat(this, "Thresh Y " + SensorName, Float.parseFloat(threshY.getText().toString()));
            sharedPreferenceHelper.putFloat(this, "Thresh Z " + SensorName, Float.parseFloat(threshZ.getText().toString()));
        }else{
            sharedPreferenceHelper.putBoolean(this, "Need Thresh " + SensorName , false);
        }
    }
    //重载单阈值方法
    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceType", "SetTextI18n"})
    private void CheckSensorOptionState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch useSensor, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch needthresh, EditText thresh, String SensorName, String Dimension) {

        if(useSensor.isChecked() && needthresh.isChecked() && !thresh.getText().toString().isEmpty()){

            ProjectInfo += "Use "+ SensorName + " : " + "true" + "\n" + "Need Thresh: " + "true " + "\r" + thresh.getText().toString() +" " + Dimension + "\n";
        }
        else if(!useSensor.isChecked()){

            ProjectInfo += "Use "+ SensorName + " : "  + "false" + "\n";
        }
        else if(useSensor.isChecked() && !needthresh.isChecked()){

            ProjectInfo += "Use "+ SensorName + " : "  + "true" + "\n" + "Need Thresh: " + "false" + "\n";
        }
        else if(useSensor.isChecked() && needthresh.isChecked() && thresh.getText().toString().isEmpty()){
            thresh.setText("9999");
            thresh.setBackground(getDrawable(R.drawable.bg_red));
            ProjectInfo += "Use "+ SensorName + " : " + "true" + "\n" + "Need Thresh: " + "true " + "\r" + thresh.getText().toString() +" " + Dimension + "\n";
        }

        if(useSensor.isChecked()){
            UseSensor = true;
            sharedPreferenceHelper.putBoolean(this, "Use " + SensorName , true);
        }else{
            sharedPreferenceHelper.putBoolean(this, "Use " + SensorName , false);
        }

        if(needthresh.isChecked()){
            sharedPreferenceHelper.putBoolean(this, "Need Thresh " + SensorName , true);
            sharedPreferenceHelper.putFloat(this, "Thresh " + SensorName, Float.parseFloat(thresh.getText().toString()));
        }else{
            sharedPreferenceHelper.putBoolean(this, "Need Thresh " + SensorName , false);
        }

    }

    /**
     * 初始化UI控件
     */
    @SuppressLint("SetTextI18n")
    private void Init(){

        //绑定控件
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        menu = findViewById(R.id.menu);

        LinearAccNeedthreshMenu = findViewById(R.id.option_linearacc_need_thresh);
        LinearAccSetthreshMenu = findViewById(R.id.option_linearacc_thresh);
        AccNeedthreshMenu = findViewById(R.id.option_acc_need_thresh);
        AccSetthreshMenu = findViewById(R.id.option_acc_thresh);
        GyroNeedthreshMenu = findViewById(R.id.option_gyro_need_thresh);
        GyroSetthreshMenu = findViewById(R.id.option_gyro_thresh);
        RotNeedthreshMenu = findViewById(R.id.option_rot_need_thresh);
        RotSetthreshMenu = findViewById(R.id.option_rot_thresh);
        MRotNeedthreshMenu = findViewById(R.id.option_mrot_need_thresh);
        MRotSetthreshMenu = findViewById(R.id.option_mrot_thresh);
        MagNeedthreshMenu = findViewById(R.id.option_mag_need_thresh);
        MagSetthreshMenu = findViewById(R.id.option_mag_thresh);
        ProximityNeedthreshMenu = findViewById(R.id.option_proximity_need_thresh);
        ProximitySetthreshMenu = findViewById(R.id.option_proximity_thresh);
        LightNeedthreshMenu = findViewById(R.id.option_light_need_thresh);
        LightSetthreshMenu = findViewById(R.id.option_light_thresh);
        PressureNeedthreshMenu = findViewById(R.id.option_pressure_need_thresh);
        PressureSetthreshMenu = findViewById(R.id.option_pressure_thresh);
        TempNeedthreshMenu = findViewById(R.id.option_temp_need_thresh);
        TempSetthreshMenu = findViewById(R.id.option_temp_thresh);
        StepNeedthreshMenu = findViewById(R.id.option_step_need_thresh);
        StepSetthreshMenu = findViewById(R.id.option_step_thresh);
        HumidityNeedthreshMenu = findViewById(R.id.option_humidity_need_thresh);
        HumiditySetthreshMenu = findViewById(R.id.option_humidity_thresh);


        //绑定CheckBox
        ProjectName = findViewById(R.id.et_projectName);

        UseLinearAcc = findViewById(R.id.cb_linearacc);
        NeedLinearAccthresh = findViewById(R.id.cb_linearacc_need_thresh);
        UseAcc = findViewById(R.id.cb_acc);
        NeedAccthresh = findViewById(R.id.cb_acc_need_thresh);
        UseGyro = findViewById(R.id.cb_gyro);
        NeedGyrothresh = findViewById(R.id.cb_gyro_need_thresh);
        UseRot = findViewById(R.id.cb_rot);
        NeedRotthresh = findViewById(R.id.cb_rot_need_thresh);
        UseMRot = findViewById(R.id.cb_mrot);
        NeedMRotthresh = findViewById(R.id.cb_mrot_need_thresh);
        UseMag = findViewById(R.id.cb_mag);
        NeedMagthresh = findViewById(R.id.cb_mag_need_thresh);
        UseProximity = findViewById(R.id.cb_proximity);
        NeedProximitythresh = findViewById(R.id.cb_proximity_need_thresh);
        UseLight = findViewById(R.id.cb_light);
        NeedLightthresh = findViewById(R.id.cb_light_need_thresh);
        UseTemp = findViewById(R.id.cb_temp);
        NeedTempthresh = findViewById(R.id.cb_temp_need_thresh);
        UsePressure = findViewById(R.id.cb_pressure);
        NeedPressurethresh = findViewById(R.id.cb_pressure_need_thresh);
        UseHumidity = findViewById(R.id.cb_humidity);
        NeedHumiditythresh = findViewById(R.id.cb_humidity_need_thresh);
        UseStep = findViewById(R.id.cb_step);
        NeedStepthresh = findViewById(R.id.cb_step_need_thresh);

        UseObjectDetection = findViewById(R.id.cb_object_detection);

        //绑定EditBox
        LinearAccthreshX = findViewById(R.id.et_linearacc_threshX);
        LinearAccthreshY = findViewById(R.id.et_linearacc_threshY);
        LinearAccthreshZ = findViewById(R.id.et_linearacc_threshZ);
        AccthreshX = findViewById(R.id.et_acc_threshX);
        AccthreshY = findViewById(R.id.et_acc_threshY);
        AccthreshZ = findViewById(R.id.et_acc_threshZ);
        GyrothreshX = findViewById(R.id.et_gyro_threshX);
        GyrothreshY = findViewById(R.id.et_gyro_threshY);
        GyrothreshZ = findViewById(R.id.et_gyro_threshZ);
        RotthreshX = findViewById(R.id.et_rot_threshX);
        RotthreshY = findViewById(R.id.et_rot_threshY);
        RotthreshZ = findViewById(R.id.et_rot_threshZ);
        MRotthreshX = findViewById(R.id.et_mrot_threshX);
        MRotthreshY = findViewById(R.id.et_mrot_threshY);
        MRotthreshZ = findViewById(R.id.et_mrot_threshZ);
        MagthreshX = findViewById(R.id.et_mag_threshX);
        MagthreshY = findViewById(R.id.et_mag_threshY);
        MagthreshZ = findViewById(R.id.et_mag_threshZ);
        Proximitythresh = findViewById(R.id.et_proximity_thresh);
        Lightthresh = findViewById(R.id.et_light_thresh);
        Tempthresh = findViewById(R.id.et_temp_thresh);
        Pressurethresh = findViewById(R.id.et_pressure_thresh);
        Humiditythresh = findViewById(R.id.et_humidity_thresh);
        Stepthresh = findViewById(R.id.et_step_thresh);

        //绑定按钮
        StartButton = findViewById(R.id.btn_start);
        //设置高斯模糊
        blurView_bottom = findViewById(R.id.blurView_bottom);
        root = findViewById(R.id.root);
        setupBlurView();

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void SetCheckBox() {
        UseLinearAcc.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseLinearAcc, LinearAccNeedthreshMenu, NeedLinearAccthresh, LinearAccSetthreshMenu));
        UseAcc.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseAcc, AccNeedthreshMenu, NeedAccthresh, AccSetthreshMenu));
        UseGyro.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseGyro, GyroNeedthreshMenu, NeedGyrothresh, GyroSetthreshMenu));
        UseRot.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseRot, RotNeedthreshMenu, NeedRotthresh, RotSetthreshMenu));
        UseMRot.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseMRot, MRotNeedthreshMenu, NeedMRotthresh, MRotSetthreshMenu));
        UseMag.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseMag, MagNeedthreshMenu, NeedMagthresh, MagSetthreshMenu));
        UseProximity.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseProximity, ProximityNeedthreshMenu, NeedProximitythresh,ProximitySetthreshMenu));
        UseLight.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseLight, LightNeedthreshMenu, NeedLightthresh, LightSetthreshMenu));
        UseTemp.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseTemp, TempNeedthreshMenu, NeedTempthresh, TempSetthreshMenu));
        UsePressure.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UsePressure, PressureNeedthreshMenu, NeedPressurethresh, PressureSetthreshMenu));
        UseHumidity.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseHumidity, HumidityNeedthreshMenu, NeedHumiditythresh, HumiditySetthreshMenu));
        UseStep.setOnCheckedChangeListener((v, isChecked) -> SetNeedthreshMenuVisibility(UseStep, StepNeedthreshMenu, NeedStepthresh, StepSetthreshMenu));
        UseObjectDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void SetNeedthreshMenuVisibility(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch UseSensor, LinearLayout NeedthreshMenu, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch Needthresh, LinearLayout SetthreshMenu) {

        Vibrate(20);

        if(UseSensor.isChecked()){
            NeedthreshMenu.setVisibility(View.VISIBLE);
            Needthresh.setOnCheckedChangeListener((buttonView, isChecked) -> {

                Vibrate(20);
                if(Needthresh.isChecked()){
                    SetthreshMenu.setVisibility(View.VISIBLE);
                }else{
                    SetthreshMenu.setVisibility(View.GONE);
                }
            });
        }else{
            Needthresh.setChecked(false);
            NeedthreshMenu.setVisibility(View.GONE);
            SetthreshMenu.setVisibility(View.GONE);
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

        ArrayList<Fragment> fragments = new ArrayList<>();
        icons = new ArrayList<>();
        if(UseSensor){
            fragments.add(new SensorRecordFragment());
            icons.add(getDrawable(R.drawable.round_sensors_36dp));
        }
        if(UseObjectDetection.isChecked()){
            fragments.add(new ObjectDetectionFragment());
            icons.add(getDrawable(R.drawable.round_cv_36dp));
        }
        fragments.add(new ProjectFragment());
        fragments.add(new UserFragment());
        icons.add(getDrawable(R.drawable.round_projects_36dp));
        icons.add(getDrawable(R.drawable.round_user_36dp));

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(this, fragments);
        viewPager.setAdapter(viewPageAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setIcon(icons.get(position))).attach();
    }

    /**
     * 设置页面滑动切换的灵敏度
     */
    public void reviseViewpagerConfigurePara() {
        try {

            //Java 中的反射（Reflection）是指在程序运行时动态地获取一个类的信息，并能够操作它的属性、方法和构造函数等。反射使得程序可以在运行时动态地加载、探索、使用编译时未知的类、调用对象的方法，以及创建对象的实例等。
            //
            //在 Java 中，反射的核心类是 java.lang.Class，每个类在运行时都有对应的 Class 对象，它包含了类的各种信息，如类的名称、属性、方法、构造函数等。通过 Class 对象，可以在程序运行时获取并操作类的各种信息。常用的反射方法包括：
            //
            //获取类的 Class 对象：可以使用 Class.forName() 方法、类字面常量（如 String.class）、对象的 getClass() 方法等方式获取一个类的 Class 对象。
            //获取类的构造函数：可以使用 Class.getDeclaredConstructors() 方法获取类的所有构造函数，再使用 Constructor.newInstance() 方法创建对象的实例。
            //获取类的方法：可以使用 Class.getDeclaredMethods() 方法获取类的所有方法，再使用 Method.invoke() 方法调用方法。
            //获取类的字段：可以使用 Class.getDeclaredFields() 方法获取类的所有字段，再使用 Field.get()、Field.set() 等方法操作字段的值。
            //反射的主要优点是使得代码更加灵活，能够在程序运行时根据需要动态地加载和调用类的各种信息，这对于一些需要扩展性强、动态性高的应用场景非常有用，比如插件系统、框架开发等。但是，反射也有一些缺点，如性能低、安全问题等，因此在使用反射时需要谨慎权衡利弊。

            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);

            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 5);//6 is empirical value
        } catch (Exception ignore) {
        }
    }

}