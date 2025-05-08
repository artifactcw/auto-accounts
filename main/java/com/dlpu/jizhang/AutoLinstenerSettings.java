package com.dlpu.jizhang;

import static android.text.TextUtils.isEmpty;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AutoLinstenerSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auto_linstener_settings);
        Switch autoListenerSwitch = (Switch) findViewById(R.id.al_autoLinstenerswitch);
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        int autolinstener = sharedPreferences.getInt("autolinstener",0);
        if (autolinstener==1){
            autoListenerSwitch.setChecked(true);
        }



        autoListenerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isChecked){
                    editor.putInt("autolinstener",1);
                    Intent intent;
                    //请求监听权限
                    if (!isListenerEnabled()) {
                        intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivity(intent);
                    }
                    //监听服务
                    intent = new Intent(buttonView.getContext(), MyNotificationListenerService.class);
                    startService(intent);
                } else {
                    editor.putInt("autolinstener",0);
                    Intent intent = new Intent(buttonView.getContext(),MyNotificationListenerService.class);
                    buttonView.getContext().stopService(intent);
                }
                editor.apply();
            }
        });

        Button bt_autoStart = findViewById(R.id.al_autostart);
        bt_autoStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            }
        });
    }

    //判断监听权限是否开启
    private boolean isListenerEnabled() {
        String pkgName = this.getPackageName();
        final String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
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

    public void bt_set(View view) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    public void bt_return(View view) {
        this.finish();
    }
}