package com.dlpu.jizhang;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.time.LocalDate;

public class TDillShow extends AppCompatActivity {

    private LinearLayout linear_scroll;
    private int date;
    private TextView tv_pay,tv_income,tv_buget,tv_surplus,tv_month;
    private View screenView;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA=500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tdil_showl);

        date = getIntent().getIntExtra("id",0);

        linear_scroll = findViewById(R.id.td_scroll_lin);
        tv_pay=findViewById(R.id.td_m_pay);
        tv_income = findViewById(R.id.td_m_income);
        tv_buget = findViewById(R.id.td_m_budget);
        tv_surplus = findViewById(R.id.td_m_surplus);
        tv_month = findViewById(R.id.td_month);
        screenView = findViewById(R.id.td_fullscreen);
        ScrollView scrollView = findViewById(R.id.td_scroll);


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("ACTION".equals(action)){
                    getdatabases("sq"+date);
                    Log.d("111111111","2");
                }
            }
        };

        IntentFilter filter = new IntentFilter("ACTION");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        Button bt_top = findViewById(R.id.td_top_bt);
        bt_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime-lastClickTime<DOUBLE_CLICK_TIME_DELTA){
                    scrollView.smoothScrollTo(0,0);
                } else {
                    lastClickTime = currentTime;
                }
            }
        });


        getdatabases("sq"+date);

        tv_month.setText(getMonth(date)+"支出");



    }

    private String getMonth(int date) {
        int month = date%100;
        switch (month){
            case 1:return "一月";
            case 2:return "二月";
            case 3:return "三月";
            case 4:return "四月";
            case 5:return "五月";
            case 6:return "六月";
            case 7:return "七月";
            case 8:return "八月";
            case 9:return "九月";
            case 10:return "十月";
            case 11:return "十一月";
            case 12:return "十二月";
            default:return "";
        }
    }

    //添加账单项目
    @SuppressLint("Range")
    public void
    getdatabases(String table){
        linear_scroll.removeAllViews();
        getMonBases();
        MydataHelper dbHelper = new MydataHelper(this);

        try {
            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
            dbHelper.openDB(); // 打开数据库
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打开数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {


            // 从 id=1 开始检索数据
            Cursor cursor = db.rawQuery("SELECT * FROM " + table + " WHERE id >= 1 ORDER BY id ASC", null);


            if (cursor != null) {
                try {
                    if (cursor.moveToLast()) {
                        String t_day = "";
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex("id"));
                            String day = cursor.getString(cursor.getColumnIndex("day"));
                            String min = cursor.getString(cursor.getColumnIndex("min"));
                            double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                            String type = cursor.getString(cursor.getColumnIndex("type"));
                            String remark = cursor.getString(cursor.getColumnIndex("remark"));
                            String IO = cursor.getString(cursor.getColumnIndex("io"));
                            RelativeLayout bill_layout = DynamicLayoutGenerator.generateBillLayout(this,
                                    min, pay, type, remark, IO,id,screenView,date);
                            View greyView = DynamicLayoutGenerator.greyView(this);

                            System.out.println(day);
                            System.out.println(t_day);

                            if (day.equals(t_day)) {
                                //从数据库中读取文件并写入
                                linear_scroll.addView(bill_layout);
                                linear_scroll.addView(greyView);
                            } else {
                                LinearLayout dayLinearLayout = DynamicLayoutGenerator.generateDateLayout(this, day + "日");
                                linear_scroll.addView(dayLinearLayout);
                                linear_scroll.addView(bill_layout);
                                linear_scroll.addView(greyView);
                            }

                            t_day = day;

                        } while (cursor.moveToPrevious());
                    }
                } finally {
                    // 关闭游标
                    cursor.close();
                }
            }

            // 关闭数据库
            dbHelper.close();
        } catch (SQLiteException e) {
            // 捕获 SQLiteException 异常
            if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                // 如果错误信息中包含“no such table”，则处理表不存在的情况
                Log.e("Database Error", "Table not found: " + e.getMessage());
                // 可以在这里添加创建表的逻辑或显示错误信息

            } else {
                // 其他 SQLite 异常
                Log.e("Database Error", "SQLite error: " + e.getMessage());
            }
        } catch (Exception e) {
            // 捕获其他异常
            Log.e("General Error", "General error: " + e.getMessage());
        }
    }


    //通过当前年月获取对应的数据
    @SuppressLint("Range")
    private void getMonBases(){
        MydataHelper dbHelper = new MydataHelper(this);
        int ym = date;
        Log.d("------p2_getYM-------", String.valueOf(ym));

        try {
            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
            dbHelper.openDB(); // 打开数据库
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打开数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        // 从 id=1 开始检索数据
        //Cursor cursor = db.rawQuery("SELECT * FROM overview WHERE id >= 1 ORDER BY id ASC", null);
        Cursor cursor = dbHelper.queryById(ym, "overview");


        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        double income = cursor.getDouble(cursor.getColumnIndex("income"));
                        double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                        double budget = cursor.getDouble(cursor.getColumnIndex("budget"));

                        updateMonViews(income,pay,budget);

                        // 输出数据到日志
                        Log.d("---page1_dbhelper-----","/"+id+"/"+income+"/"+pay+"/"+budget);

                    } while (cursor.moveToNext());
                }
            } finally {
                // 关闭游标
                cursor.close();
            }
        }

        // 关闭数据库
        dbHelper.close();
    }

    private void updateMonViews(double income, double pay, double budget) {
        tv_income.setText(String.format("%.2f",income));
        tv_pay.setText(String.format("%.2f",pay));
        tv_buget.setText(String.format("%.2f",budget));
        tv_surplus.setText(String.format("%.2f",income-pay));

    }

    //返回键
    public void bt_return(View view) {
        this.finish();
    }
}