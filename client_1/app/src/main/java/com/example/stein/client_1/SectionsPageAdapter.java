package com.example.stein.client_1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stein on 25/01/2018.
 */



public class SectionsPageAdapter extends FragmentPagerAdapter {
    public List<Fragment> mFragmentList = new ArrayList<Fragment>();
    public List<String> mFragmentTitleList = new ArrayList<String>();
    private static final String TAG = "SectionsPageAdapter";

    public void addFragment(Fragment fragment, String title) {
        Log.d(TAG,"addFragment:" + title);
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }


    public SectionsPageAdapter(FragmentManager fm){

        super(fm);
    }




    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG,"getPageTitle: got page title in position:"+String.valueOf(position) + "-" + mFragmentTitleList.get(position));
        return mFragmentTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG,"getItem: got page fregment in position:"+String.valueOf(position)+"-name-"+mFragmentTitleList.get(position));
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }





}


