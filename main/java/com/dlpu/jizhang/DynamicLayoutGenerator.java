package com.dlpu.jizhang;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class DynamicLayoutGenerator{
    private static String p_type="未知";
    private static int iD;
    private static int DATE;

    @SuppressLint("Range")
    public static RelativeLayout generateBillLayout(Context context,
                                                    String min,
                                                    Double pay, String type, String remark,String IO,int id
            ,View sreenView,int date) {
        iD=id;
        DATE=date;
        // 创建 RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        relativeLayout.setPadding(5, 5, 5, 5);
        relativeLayout.setBackgroundColor(Color.WHITE);

        // 创建类型 TextView
        TextView typeTextView = new TextView(context);
        typeTextView.setId(View.generateViewId());
        RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        reLayoutParams.setMarginStart(20);
        typeTextView.setLayoutParams(reLayoutParams);
        typeTextView.setText(type);
        typeTextView.setTextColor(Color.BLACK);
        typeTextView.setTextSize(30);

        // 创建备注与时间的 LinearLayout
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(View.generateViewId());
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                600,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1.0f);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;

        // 添加备注 TextView 到 LinearLayout
        TextView remarkTextView = new TextView(context);
        remarkTextView.setLayoutParams(layoutParams);
        remarkTextView.setText(remark);
        remarkTextView.setMaxLines(1);
        remarkTextView.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(remarkTextView);

        // 添加时间 TextView 到 LinearLayout
        TextView timeTextView = new TextView(context);
        timeTextView.setLayoutParams(layoutParams);
        timeTextView.setText(min);
        linearLayout.addView(timeTextView);

        // 设置 LinearLayout 的位置
        RelativeLayout.LayoutParams linearLayoutParams = new RelativeLayout.LayoutParams(
                linearLayout.getLayoutParams());
        linearLayoutParams.addRule(RelativeLayout.END_OF, typeTextView.getId());
        linearLayoutParams.setMarginStart(24);
        linearLayout.setLayoutParams(linearLayoutParams);

        // 创建支付数目 TextView
        TextView amountTextView = new TextView(context);
        amountTextView.setId(View.generateViewId());
        amountTextView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        amountTextView.setGravity(Gravity.CENTER);
        if (IO.equals("in")){
            amountTextView.setTextColor(Color.GREEN);
            amountTextView.setText("+"+pay);
        } else {
            amountTextView.setTextColor(Color.RED);
            amountTextView.setText("-"+pay);
        }
        amountTextView.setTextSize(30);

        Button editButton = new Button(context);
        editButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams eBt = new RelativeLayout.LayoutParams(50, 50);
        eBt.addRule(RelativeLayout.ALIGN_PARENT_END);
        eBt.setMarginEnd(15);
        eBt.setLayoutDirection(Gravity.CENTER_VERTICAL);
//        editButton.setText("编辑");
        editButton.setTextSize(20);
        editButton.setBackground(ContextCompat.getDrawable(context,R.drawable.editor));
        editButton.setLayoutParams(eBt);

        final int Id = id;

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = id;
                System.out.println(i);
                sreenView.setVisibility(View.VISIBLE);

                // 获取当前的视图的上下文（context）
                Context context = v.getContext();
                // 获取 LayoutInflater 实例
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // 使用 inflater 来加载布局文件
                View popupLayout = inflater.inflate(R.layout.pop_td_editor, null);
                PopupWindow popupWindow = new PopupWindow(popupLayout,
                        ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);

                popupWindow.showAtLocation(v,Gravity.CENTER,0,0);
                View popView = popupWindow.getContentView();

                LinearLayout linear = popView.findViewById(R.id.pop_td_editlayout);

                Button bt_type = popView.findViewById(R.id.pop_td_1);
                GridLayout gridLayout = popView.findViewById(R.id.pop_td_grid);
                LinearLayout gridLinear = popView.findViewById(R.id.pop_td_lineargrid);
                bt_type.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linear.setVisibility(View.GONE);
                        if (gridLinear.getVisibility()==View.GONE) {
                            gridLinear.setVisibility(View.VISIBLE);
                            MydataHelper dbHelper = new MydataHelper(v.getContext());
                            try {
                                dbHelper.createDB();
                                dbHelper.openDB();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String io = dbHelper.getIOById("sq" + date, id);
                            if (io.equals("out")) {
                                addCheckBox(popView, gridLayout, stringManager.getOption_out());
                            } else {
                                addCheckBox(popView, gridLayout, stringManager.getOption_in());
                            }
                        } else {
                            gridLinear.setVisibility(View.GONE);
                        }

                    }
                });
                Button bt_pop_submit = popView.findViewById(R.id.pop_td_gridsubmit);
                bt_pop_submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MydataHelper dbHelper = new MydataHelper(v.getContext());
                        try {
                            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                            dbHelper.openDB(); // 打开数据库
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Cursor cursor = dbHelper.queryById(Id,"sq"+date);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                int id = cursor.getInt(cursor.getColumnIndex("id"));
                                String day = cursor.getString(cursor.getColumnIndex("day"));
                                String min = cursor.getString(cursor.getColumnIndex("min"));
                                double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                                String type = p_type;
                                String remark = cursor.getString(cursor.getColumnIndex("remark"));
                                String IO = cursor.getString(cursor.getColumnIndex("io"));

                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("day", day);
                                values.put("min",min);
                                values.put("type",type);
                                values.put("pay", pay);
                                values.put("remark",remark);
                                values.put("io",IO);

                                int rowsAffected = db.update("sq"+date, values, "id = ?", new String[]{String.valueOf(id)});


                            } while (cursor.moveToNext());
                        }
                        Intent intent = new Intent("ACTION");
                        intent.putExtra("key","value");
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                        localBroadcastManager.sendBroadcast(intent);
                        popupWindow.dismiss();
                    }
                });

                Button bt_remark = popView.findViewById(R.id.pop_td_2);
                bt_remark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gridLinear.setVisibility(View.GONE);
                        if (linear.getVisibility()==View.GONE){
                            linear.setVisibility(View.VISIBLE);
                        } else {
                            linear.setVisibility(View.GONE);
                        }

                    }
                });

                Button bt_pop_enter = popView.findViewById(R.id.pop_td_enter);
                bt_pop_enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pop_edit = popupWindow.getContentView().findViewById(R.id.pop_td_edittext);
                        String new_text = pop_edit.getText().toString();
                        MydataHelper dbHelper = new MydataHelper(v.getContext());
                        try {
                            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                            dbHelper.openDB(); // 打开数据库
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Cursor cursor = dbHelper.queryById(Id,"sq"+date);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                int id = cursor.getInt(cursor.getColumnIndex("id"));
                                String day = cursor.getString(cursor.getColumnIndex("day"));
                                String min = cursor.getString(cursor.getColumnIndex("min"));
                                double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                                String type = cursor.getString(cursor.getColumnIndex("type"));
                                String remark = new_text;
                                String IO = cursor.getString(cursor.getColumnIndex("io"));

                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("day", day);
                                values.put("min",min);
                                values.put("type",type);
                                values.put("pay", pay);
                                values.put("remark",remark);
                                values.put("io",IO);

                                int rowsAffected = db.update("sq"+date, values, "id = ?", new String[]{String.valueOf(id)});


                            } while (cursor.moveToNext());
                        }
                        Intent intent = new Intent("ACTION");
                        intent.putExtra("key","value");
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                        localBroadcastManager.sendBroadcast(intent);
                        popupWindow.dismiss();
                    }
                });


                Button bt_delete = popView.findViewById(R.id.pop_td_delete);
                bt_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MydataHelper dbHelper = new MydataHelper(v.getContext());
                        try {
                            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                            dbHelper.openDB(); // 打开数据库
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Cursor cursor = dbHelper.queryById(Id,"sq"+date);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                String IO = cursor.getString(cursor.getColumnIndex("io"));
                                // 其他操作
                                if (IO.equals("out")){
                                    double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                                    dbHelper.changetablebases(date,pay,1,"out");
                                }else {
                                    double income = cursor.getDouble(cursor.getColumnIndex("pay"));
                                    dbHelper.changetablebases(date,income,1,"in");
                                }
                            } while (cursor.moveToNext());
                        }


                        //删除账单
                        dbHelper.deleteAndUpdateIds("sq"+date,Id);
                        popupWindow.dismiss();

                        Intent intent = new Intent("ACTION");
                        intent.putExtra("key","value");
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                        localBroadcastManager.sendBroadcast(intent);
                        Log.d("111111111","1");
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        sreenView.setVisibility(View.GONE);
                    }
                });
            }
        });

        // 设置支付数目 TextView 的位置
        RelativeLayout.LayoutParams amountLayoutParams = new RelativeLayout.LayoutParams(
                amountTextView.getLayoutParams());
        amountLayoutParams.addRule(RelativeLayout.START_OF,editButton.getId());
        amountLayoutParams.setMarginEnd(30);
        amountTextView.setLayoutParams(amountLayoutParams);

        // 添加视图到 RelativeLayout 容器
        relativeLayout.addView(typeTextView);
        relativeLayout.addView(linearLayout);
        relativeLayout.addView(editButton);
        relativeLayout.addView(amountTextView);

        return relativeLayout;
    }

    public static LinearLayout generateDateLayout(Context context, String date) {
        // 创建 LinearLayout 容器
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 创建 TextView
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMarginStart(20);
        textView.setLayoutParams(linearLayoutParams);
        textView.setText(date);
        textView.setPadding(10, 0, 0, 0); // 设置左边距

        // 添加 TextView 到 LinearLayout
        linearLayout.addView(textView);

        return linearLayout;
    }
    public static View greyView(Context context){
        View view = new View(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,5);
        view.setLayoutParams(layoutParams);
        return view;
    }


    //添加单选框按钮
    @SuppressLint("Range")
    private static void addCheckBox(View v, GridLayout gridLayout, String[] options){
        gridLayout.removeAllViews();

        for (String s:options){
            //ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(),R.style.RadioButtonStyle);
            RadioButton radioButton = new RadioButton(v.getContext());
            radioButton.setText(s); // 设置文本
            radioButton.setTextSize(15); // 设置文本大小
            radioButton.setGravity(Gravity.CENTER); // 设置文本居中
            radioButton.setBackgroundResource(R.drawable.check); // 设置背景
            radioButton.setButtonDrawable(null);
            GridLayout.LayoutParams layoutParams =new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            layoutParams.width=100;
            layoutParams.height=100;
            layoutParams.setMargins(20,20,20,20);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setId(View.generateViewId()); // 为每个RadioButton生成唯一ID

            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked){
                    for (int i=0;i<gridLayout.getChildCount();i++){
                        View child = gridLayout.getChildAt(i);
                        if (child instanceof RadioButton && child!=buttonView){
                            ((RadioButton) child).setChecked(false);
                        }
                    }
                    p_type = (String) buttonView.getText();
                    Log.d("pop_td_grid",p_type);
//                    System.out.println(radio);
                    MydataHelper dbHelper = new MydataHelper(v.getContext());
                    try {
                        dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                        dbHelper.openDB(); // 打开数据库
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Cursor cursor = dbHelper.queryById(iD,"sq"+DATE);

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex("id"));
                            String day = cursor.getString(cursor.getColumnIndex("day"));
                            String min = cursor.getString(cursor.getColumnIndex("min"));
                            double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                            String type = p_type;
                            String remark = cursor.getString(cursor.getColumnIndex("remark"));
                            String IO = cursor.getString(cursor.getColumnIndex("io"));

                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("day", day);
                            values.put("min",min);
                            values.put("type",type);
                            values.put("pay", pay);
                            values.put("remark",remark);
                            values.put("io",IO);

                            int rowsAffected = db.update("sq"+DATE, values, "id = ?", new String[]{String.valueOf(id)});


                        } while (cursor.moveToNext());
                    }
                    Intent intent = new Intent("ACTION");
                    intent.putExtra("key","value");
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(v.getContext());
                    localBroadcastManager.sendBroadcast(intent);
                }
            });

            // 添加到GridLayout
            gridLayout.addView(radioButton);
        }
    }

    public static View createMVLayout(Context context,int id,Double pay,Double income,Double budget) {
        // 创建父LinearLayout
        LinearLayout parentLayout = new LinearLayout(context);
        parentLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        parentLayout.setOrientation(LinearLayout.VERTICAL);
        parentLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
        parentLayout.setPadding(0, 5, 0, 0); // 顶部5dp的边距

        // 创建顶级RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        relativeLayout.setPadding(0, 10, 0, 0); // 顶部10dp的边距

        // 添加"十月支出"的TextView
        TextView monthText = new TextView(context);
        monthText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        monthText.setId(View.generateViewId());
        monthText.setText(getMonth(id)+"支出");
        monthText.setTextColor(Color.BLACK);
        monthText.setTextSize(30);
        monthText.setPadding(10, 0, 0, 0); // 左边10dp的边距
        relativeLayout.addView(monthText);

        // 添加支出金额的TextView
        TextView monthPayText = new TextView(context);
        RelativeLayout.LayoutParams payLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        payLayoutParams.addRule(RelativeLayout.END_OF, monthText.getId());
        payLayoutParams.setMargins(17, 0, 0, 0); // 左边17dp的边距
        monthPayText.setLayoutParams(payLayoutParams);
        monthPayText.setText(String.format("%.2f",pay));
        monthPayText.setTextColor(ContextCompat.getColor(context, R.color.red));
        monthPayText.setTextSize(40);
        relativeLayout.addView(monthPayText);

        // 添加进入按钮
        Button enterButton = new Button(context);
        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(
                60, // 宽25dp
                50 // 高20dp
        );
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonLayoutParams.setMarginEnd(20);
        enterButton.setLayoutParams(buttonLayoutParams);
        // 这里假设你有一个名为"mon_more"的背景资源
         enterButton.setBackgroundResource(R.drawable.mon_more);
         enterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(),TDillShow.class);
                 intent.putExtra("id",id);
                 v.getContext().startActivity(intent);
             }
         });
        relativeLayout.addView(enterButton);

        parentLayout.addView(relativeLayout);

        // 创建收入和结余的水平LinearLayout
        LinearLayout incomeSurplusLayout = new LinearLayout(context);
        incomeSurplusLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        incomeSurplusLayout.setOrientation(LinearLayout.HORIZONTAL);
        incomeSurplusLayout.setPadding(0, 10, 0, 10); // 上下各10dp的边距

        TextView incomeLabel = new TextView(context);
        incomeLabel.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        incomeLabel.setText("收入:");
        incomeLabel.setTextColor(Color.BLACK);
        incomeLabel.setTextSize(20);
        incomeLabel.setPadding(5, 0, 0, 0); // 左边5dp的边距
        incomeSurplusLayout.addView(incomeLabel);

        TextView incomeText = new TextView(context);
        incomeText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        incomeText.setText(String.format("%.2f",income));
        incomeText.setTextColor(ContextCompat.getColor(context, R.color.income));
        incomeText.setTextSize(23);
        incomeText.setPadding(7, 0, 0, 0); // 左边7dp的边距
        incomeSurplusLayout.addView(incomeText);

        TextView surplusLabel = new TextView(context);
        surplusLabel.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        surplusLabel.setText("结余:");
        surplusLabel.setTextColor(Color.BLACK);
        surplusLabel.setTextSize(20);
        surplusLabel.setPadding(15, 0, 0, 0); // 左边15dp的边距
        incomeSurplusLayout.addView(surplusLabel);

        TextView surplusText = new TextView(context);
        surplusText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        surplusText.setText(String.format("%.2f",income-pay));
        surplusText.setTextColor(ContextCompat.getColor(context, R.color.surplus));
        surplusText.setTextSize(23);
        surplusText.setPadding(5, 0, 0, 0); // 左边5dp的边距
        incomeSurplusLayout.addView(surplusText);

        parentLayout.addView(incomeSurplusLayout);

        // 创建预算的水平LinearLayout
        LinearLayout budgetLayout = new LinearLayout(context);
        budgetLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        budgetLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView budgetLabel = new TextView(context);
        budgetLabel.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        budgetLabel.setText("预算:");
        budgetLabel.setTextColor(Color.BLACK);
        budgetLabel.setTextSize(20);
        budgetLabel.setPadding(5, 0, 0, 0); // 左边5dp的边距
        budgetLayout.addView(budgetLabel);

        View n_view = new View(context);
        LinearLayout.LayoutParams n_params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,10
        );
        n_view.setLayoutParams(n_params);

        TextView budgetText = new TextView(context);
        budgetText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        budgetText.setText(String.format("%.2f",budget));
        budgetText.setTextColor(ContextCompat.getColor(context, R.color.blue));
        budgetText.setTextSize(23);
        budgetText.setPadding(5, 0, 0, 0); // 左边5dp的边距
        budgetLayout.addView(budgetText);

        parentLayout.addView(budgetLayout);


        return parentLayout;
    }

    private static String getMonth(int date) {
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
}
