package hienlt.hutech.iotapp.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by hienl on 12/15/2016.
 */

public class FragmentUtils {
    public static void replace(FragmentManager manager, int layoutId, Fragment fragment){
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(layoutId,fragment);
        transaction.commit();
    }
}
