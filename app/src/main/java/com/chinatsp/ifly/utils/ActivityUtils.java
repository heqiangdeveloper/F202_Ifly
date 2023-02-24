package com.chinatsp.ifly.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.chinatsp.ifly.R;

public class ActivityUtils {

    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                              @NonNull Fragment fragment, int frameId) {
        Utils.checkNotNull(fragmentManager);
        Utils.checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }
    public static void replaceFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                              @NonNull Fragment fragment, int frameId) {
        Utils.checkNotNull(fragmentManager);
        Utils.checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commit();
    }

    public static void replaceFragmentToActivity2 (@NonNull FragmentManager fragmentManager,
                                                  @NonNull Fragment fragment, int frameId) {
        Utils.checkNotNull(fragmentManager);
        Utils.checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commitAllowingStateLoss ();
    }

    public static void removeFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                                  @NonNull Fragment fragment, int frameId) {
        Utils.checkNotNull(fragmentManager);
        Utils.checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }

    public static void showFragment(Fragment fragment1, Fragment fragment2,FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager
                .beginTransaction();
        if (!fragment2.isAdded()) {
            transaction.add(R.id.framelayout_content, fragment2).remove(fragment1)
                    .commitAllowingStateLoss();

        } else {
            transaction
                    .remove(fragment1)
                    .show(fragment2)
                    .commitAllowingStateLoss();
        }
    }
}
