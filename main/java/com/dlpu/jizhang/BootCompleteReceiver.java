package com.dlpu.jizhang;

import static android.text.TextUtils.isEmpty;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 在这里启动你的应用
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
            int autolinstener = sharedPreferences.getInt("autolinstener",0);

            if (autolinstener==1){
                //请求监听权限
                if (!isListenerEnabled(context)){
                    intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    context.startActivity(intent);
                }
                //监听服务
                intent = new Intent(context,MyNotificationListenerService.class);
                context.startService(intent);
            } else {
                intent = new Intent(context,MyNotificationListenerService.class);
                context.stopService(intent);
            }

//
//            Intent appIntent = new Intent(context, MainActivity.class);
//            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(appIntent);
        }
    }

    //判断监听权限是否开启
    private boolean isListenerEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}