package com.example.senner.UI;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPageAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> fragments;

    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Fragment> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;//把fragments传入MyFragmentStateAdapter
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);//这里记得要重写，默认是返回Null的
    }

    @Override
    public int getItemCount() {
        return fragments.size();//这里也改
    }
}
