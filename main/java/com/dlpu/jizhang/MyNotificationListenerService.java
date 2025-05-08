package com.dlpu.jizhang;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"监听服务运行中", Toast.LENGTH_SHORT).show();
        // Service logic here
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // 当有新通知时调用
        Notification notification = sbn.getNotification();
        // 获取通知内容
        String notificationContent = notification.extras.getString(Notification.EXTRA_TEXT);
        // 在这里处理通知，例如获取通知内容
        String packageName = sbn.getPackageName();
        String notificationKey = sbn.getKey();
        // 你可以在这里添加自己的逻辑来处理通知
        if (notificationContent !=null && packageName.equals("com.tencent.mm")){
//            Toast.makeText(this,packageName,Toast.LENGTH_LONG).show();
            if  (notificationContent.contains("支付")) {
                String money = match_num(notificationContent);
                if (!Objects.equals(money, "")) {

                    MydataHelper dbHelper = new MydataHelper(this);

                    try {
                        dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                        dbHelper.openDB(); // 打开数据库
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String p_type = "自动记录";

                    dbHelper.createTableAndInsertData("sq" + getYM(), getNowDay(),
                            getNowMin(), Double.valueOf(money), p_type, "", "out");
                    dbHelper.changetablebases(getYM(), Double.valueOf(money), 0, "out");




                    Toast.makeText(this,"已记录:" + money + "元", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent("ACTION");
                    intent.putExtra("key","value");
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // 当通知被移除时调用
    }

    @Override
    public void onDestroy() {
    }


    public String match_num(String str){
        int havePoint=0;
        String input = str;
        StringBuilder number = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch)) {
                number.append(ch);
            } else if (Objects.equals(ch,'.')) {
                havePoint = 1;
                number.append(ch);

            } else {
                if (havePoint==1) {
                    break;
                    // 清空StringBuilder
                } else {
                    number.setLength(0);
                }
            }
        }
        return number.toString();
    }

    private int getYM(){
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        return year*100+month;
    }

    //获取当前天数
    private String getNowDay(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        return today.format(formatter);
    }

    private String getNowMin(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return time.format(formatter);
    }
}
