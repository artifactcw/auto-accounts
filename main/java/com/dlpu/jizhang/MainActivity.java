package com.dlpu.jizhang;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.text.TextUtils.isEmpty;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FrameLayout mainFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.main_bottombar);
        mainFrameLayout = findViewById(R.id.main_fram);


        bottomNavigationView = findViewById(R.id.main_bottombar);
        mainFrameLayout = findViewById(R.id.main_fram);

        //存储权限
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        int autolinstener = sharedPreferences.getInt("autolinstener",0);

        if (autolinstener==1){
            Intent intent;
            //请求监听权限
            if (!isListenerEnabled()){
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
            //监听服务
            intent = new Intent(this,MyNotificationListenerService.class);
            startService(intent);
        } else {
            Intent intent = new Intent(this,MyNotificationListenerService.class);
            stopService(intent);
        }


        //初始加载page1
        loadFragment(new Page1());

        //设置bottombar的监听事件
        bottomNavigationView.setOnItemSelectedListener(item->{
            return choose_bar(item.getItemId());
        });
    }

    //加载fragment
    private void loadFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fram,fragment)
                .commit();
    }

    private boolean choose_bar(int id){
        Fragment fragment = null;

        if (id==R.id.bottombar_1){
            fragment = new Page1();
        }else if (id==R.id.bottombar_2){
            fragment = new Page2();
        }else if(id==R.id.bottombar_3){
            fragment = new Page3();
        }
        loadFragment(fragment);
        return true;
    }

    //判断监听权限是否开启
    private boolean isListenerEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
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