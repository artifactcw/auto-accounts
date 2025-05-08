package com.dlpu.jizhang;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MonViewActivity extends AppCompatActivity {

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mon_view);

        LinearLayout linearLayout = findViewById(R.id.mv_gridLinear);
        MydataHelper dbHelper = new MydataHelper(this);

        try {
            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
            dbHelper.openDB(); // 打开数据库
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor cursor = dbHelper.getReadableDatabase().query("overview",null,null,null,null,null,null);
        if (cursor != null && cursor.moveToLast()){
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double income = cursor.getDouble(cursor.getColumnIndex("income"));
                double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                double budget = cursor.getDouble(cursor.getColumnIndex("budget"));

                linearLayout.addView(DynamicLayoutGenerator.createMVLayout(this,
                        id,pay,income,budget));

                View n_view = new View(this);
                LinearLayout.LayoutParams n_params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,20
                );
                n_view.setLayoutParams(n_params);
                linearLayout.addView(n_view);

            }while (cursor.moveToPrevious());
            cursor.close();
        }

       // linearLayout.addView(DynamicLayoutGenerator.createMVLayout(this,202505,30.0,1000.0,500.0));

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

    public void bt_return(View view) {
        this.finish();
    }
}