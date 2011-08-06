package com.m039.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created: Thu Aug  4 23:58:55 2011
 *
 * @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
 * @version 1.0
 */
public class MDBProvider extends AppWidgetProvider {
    private static final String          TAG             = "MDBWidget";
    private static final String          PREFS           = "com.m039.study.MDBWidget";
    private static final String          WIDGET_UPDATE   = "com.m039.study.WIDGET_UPDATE";
    private static final String          WIDGET_CLICK    = "com.m039.study.WIDGET_CLICK";

    @Override
    public void             onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void             onDisabled(Context context) {
        super.onDisabled(context);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createUpdateIntent(context));
    }

    @Override
    public void             onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (WIDGET_UPDATE.equals(action)) {
            RemoteViews         views   = getViews(context);
            ComponentName       widget  = new ComponentName(context.getPackageName(),
                                                            getClass().getName());
            AppWidgetManager    manager = AppWidgetManager.getInstance(context);

            for (int id: manager.getAppWidgetIds(widget)) {
                updateWidgetText(context, views);
                manager.updateAppWidget(id, views);
            }
        }

        if (WIDGET_CLICK.equals(action)) {
            if (MobileData.isEnable(context)) {

                Toast.makeText(context, "Disabling mobile data.", Toast.LENGTH_SHORT).show();
                MobileData.disable(context);

                saveMobileStatus(context, false); // save a state

            } else {

                Toast.makeText(context, "Enabling mobile data.", Toast.LENGTH_SHORT).show();
                MobileData.enable(context);

                saveMobileStatus(context, true); // save a state
            }
        }
    }

    @Override
    public void             onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // restore a previous state

        if (!restoreMobileStatus(context)) {
            Log.d(TAG, "Previous state is 'disable' [false]");
            MobileData.disable(context);
        } else {
            Log.d(TAG, "Previous state is 'enable' [true]");
        }

        // intialiazie

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC,
                                  System.currentTimeMillis() + 1000,
                                  1000,
                                  createUpdateIntent(context));

        RemoteViews views = getViews(context);

        for (int appWidgetId: appWidgetIds) {
            updateWidgetClick(context, views);
            updateWidgetText(context, views);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private RemoteViews     getViews(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.md_button);
    }

    // update widget states

    private void            updateWidgetText(Context context, RemoteViews views) {
        if (MobileData.isEnable(context)) {
            views.setCharSequence(R.id.md_button, "setText",
                                  Html.fromHtml("Mobile Data <font color='#00FF00'>Enabled</font>"));
        } else {
            views.setCharSequence(R.id.md_button, "setText",
                                  Html.fromHtml("Mobile Data <font color='#FF0000'>Disabled</font>"));
        }
    }

    private void            updateWidgetClick(Context context, RemoteViews views) {
        views.setOnClickPendingIntent(R.id.md_button, createClickIntent(context));
    }

    // intents

    private PendingIntent   createUpdateIntent(Context context) {
        Intent intent               = new Intent(WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                                                 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent   createClickIntent(Context context) {
        Intent intent               = new Intent(WIDGET_CLICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                                                 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    // preference

    private boolean         restoreMobileStatus(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, 0);
        return sp.getBoolean("isEnabled", true);
    }

    private void            saveMobileStatus(Context context) {
        saveMobileStatus(context, MobileData.isEnable(context));
    }

    private void            saveMobileStatus(Context context, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, 0);
        Editor editor = sp.edit();

        editor.putBoolean("isEnabled", value);

        if (editor.commit()) {
            Log.d(TAG, "Mobile data state was stored successfuly.");
        } else {
            Log.d(TAG, "Mobile data state wasn't stored successfuly.");
        }
    }
}