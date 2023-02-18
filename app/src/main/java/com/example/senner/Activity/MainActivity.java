package com.example.senner.Activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

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

    private BlurView blurView_title;
    private BlurView blurView_bottom;
    private ViewGroup root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置沉浸式状态栏
        ImmersionBar
                .with(this)
                .init();
        Init();
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

        //设置高斯模糊
        blurView_title = findViewById(R.id.blurView_title);
        blurView_bottom = findViewById(R.id.blurView_bottom);
        root = findViewById(R.id.root);
        setupBlurView();

    }
    /**
     * 设置高斯模糊
     */
    private void setupBlurView() {

        final float radius = 25f;

        final Drawable windowBackground = getWindow().getDecorView().getBackground();
        BlurAlgorithm algorithm = getBlurAlgorithm();

        blurView_title.setupWith(root, algorithm)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);

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