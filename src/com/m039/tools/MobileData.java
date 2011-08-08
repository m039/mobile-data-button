package com.m039.tools;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This class allows you to turn on and off mobile data connection.
 *
 * All methods in this class are static, so you can just grab this
 * file and use it.
 *
 * Created: Fri Aug  5 12:26:39 2011
 *
 * @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
 * @version 1.0
 */
public class MobileData {
    /**
     * Checks if mobile data is enabled.
     *
     * @param context a reference to Context or Activity
     * @return true if data enabled.
     */
    public static boolean   isEnable(Context context) {
        TelephonyManager tmanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (tmanager.getDataState() == TelephonyManager.DATA_CONNECTED);
    }

    /**
     * Turn off mobile data.
     *
     * @param context a reference to Context or Activity
     */
    public static void      disable(Context context) {
        switchState(context, false);
    }

    /**
     * Turn on mobile data.
     *
     * @param context a reference to Context or Activity
     */
    public static void      enable(Context context) {
        switchState(context, true);
    }   

    private static void     switchState(Context context, boolean on) {
        Method dataConnSwitchmethod;
        Object ITelephonyStub;

        TelephonyManager tmanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        
        try {
            Method getITelephonyMethod = tmanager.getClass().getDeclaredMethod("getITelephony");

            getITelephonyMethod.setAccessible(true);
            ITelephonyStub = getITelephonyMethod.invoke(tmanager);

            if (on) {
                dataConnSwitchmethod = ITelephonyStub.getClass().getDeclaredMethod("enableDataConnectivity");
            } else {
                dataConnSwitchmethod = ITelephonyStub.getClass().getDeclaredMethod("disableDataConnectivity");
            }

            dataConnSwitchmethod.setAccessible(true);
            dataConnSwitchmethod.invoke(ITelephonyStub);
            
        } catch (Exception e) {
            Log.d("m039", "Something goes wrong!");
        }
    }
}
