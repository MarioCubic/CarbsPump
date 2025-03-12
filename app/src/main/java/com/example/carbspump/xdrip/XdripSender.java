package com.example.carbspump.xdrip;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


import com.example.carbspump.Const;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;


import com.eveningoutpost.dexdrip.services.broadcastservice.models.Settings;


public class XdripSender {

    protected static final String XDRIP_RECEIVER = "com.eveningoutpost.dexdrip.watch.wearintegration.BROADCAST_SERVICE_RECEIVER";
    private static final String XDRIP_JOB_NAME = "com.example.carbspump";




    public static void registerAppWithXDrip(Context context) {
        if (context == null){
            return;
        }
        Long defValue = 0L;
        Settings settings = new Settings();
        settings.setApkName(XDRIP_JOB_NAME);
        settings.setGraphStart(defValue);
        settings.setGraphEnd(defValue);
        settings.setDisplayGraph(false);

        Intent intent = new Intent(XDRIP_RECEIVER);
        intent.setPackage("com.eveningoutpost.dexdrip");
        intent.putExtra(Const.INTENT_PACKAGE_KEY, XDRIP_JOB_NAME);
        intent.putExtra(Const.INTENT_FUNCTION_KEY, Const.CMD_SET_SETTINGS);
        intent.putExtra(Const.INTENT_SETTINGS, settings);
        context.sendBroadcast(intent);

    }

    public static boolean isXDripInstalled(PackageManager pm) {

        try {
            pm.getPackageInfo("com.eveningoutpost.dexdrip", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static void sendToXDrip(Context context, double insulin, double carbs, long time) {
        if(context == null) {
            return;
        }

                // Создаем Intent для xDrip
                long timeStamp = System.currentTimeMillis();
                Intent intent = new Intent(XDRIP_RECEIVER);
                intent.setPackage("com.eveningoutpost.dexdrip");
                intent.putExtra(Const.INTENT_PACKAGE_KEY, XDRIP_JOB_NAME);
                intent.putExtra(Const.INTENT_FUNCTION_KEY, Const.CMD_ADD_TREATMENT);
                intent.putExtra("timeStamp", time);
                intent.putExtra("carbs", carbs);
                intent.putExtra("insulin", insulin);
                // Отправляем Broadcast
        Log.d("Dialog", "Отправил еба");
                context.sendBroadcast(intent);


    }
    }

