package com.dlpu.jizhang;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Page1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Page1 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView tv_m_pay,tv_m_income,tv_m_surplus,tv_m_budget,tv_m_perpay,tv_m_superpay;
    private TextView tv_t_pay,tv_t_surplus;
    private TextView tv_month;
    private double m_pay,m_income,m_surplus,
            m_budget,m_superpay,m_perpay;
    private double t_pay,t_surplus;
    private Button bt_bugetEditor;
    private View screenView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Page1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Page1.
     */
    // TODO: Rename and change types and number of parameters
    public static Page1 newInstance(String param1, String param2) {
        Page1 fragment = new Page1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page1, container, false);

        tv_m_pay=view.findViewById(R.id.p1_m_pay);//月花销
        tv_m_income = view.findViewById(R.id.p1_m_income);//月收入
        tv_m_surplus = view.findViewById(R.id.p1_m_surplus);//月结余
        tv_m_budget = view.findViewById(R.id.p1_m_budget);//月预算
        tv_m_perpay = view.findViewById(R.id.p1_m_perpay);//月日均消费
        tv_m_superpay = view.findViewById(R.id.p1_m_superpay);//月剩余日均消费
        tv_month = view.findViewById(R.id.p1_month);
        tv_t_pay=view.findViewById(R.id.p1_t_pay);
        tv_t_surplus = view.findViewById(R.id.p1_t_surplus);
        bt_bugetEditor = view.findViewById(R.id.p1_bugetEditor);
        screenView = view.findViewById(R.id.p1_allscreen);


        //更改标题
        tv_month.setText(getMonth()+"支出");

        openDB(getContext());

        t_pay = getTPay(view.getContext());
        t_surplus = m_superpay-t_pay;

        tv_t_pay.setText(String.format("%.2f",t_pay));
        tv_t_surplus.setText(String.format("%.2f",t_surplus));

        //进入按钮的点击事件
        Button bt_enter = view.findViewById(R.id.p1_enter);
        bt_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),TDillShow.class);
                intent.putExtra("id",getYM());
                startActivity(intent);
            }
        });

        //更新今日消费
        tv_t_pay.setText(String.format("%.2f",t_pay));

        //预算编辑的按钮点击事件
        bt_bugetEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取弹窗布局
                screenView.setVisibility(View.VISIBLE);
                View popupLayout = getLayoutInflater().inflate(R.layout.pop_p1_bugeteditor,null);
                PopupWindow popupWindow = new PopupWindow(popupLayout,
                        ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,
                        true);
                popupWindow.setFocusable(true);
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                //弹出弹窗
                popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
                View popView = popupWindow.getContentView();
                Button submit = popView.findViewById(R.id.pop_p1_enter);
                EditText editText = popView.findViewById(R.id.pop_p1_edittext);
                editText.requestFocus();

                //异步调出键盘
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT);
                    }
                },100);

                //提交按钮点击事件
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String budget = editText.getText().toString();
                        MydataHelper dbHelper = new MydataHelper(getContext());
                        try {
                            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                            dbHelper.openDB(); // 打开数据库
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dbHelper.changeBudget(getYM(), Double.valueOf(budget));

                            openDB(getContext());
                            t_surplus = m_superpay - t_pay;
                            tv_t_surplus.setText(String.format("%.2f", t_surplus));
                        } catch (Exception e){

                        }

                        screenView.setVisibility(View.GONE);
                        popupWindow.dismiss();
                    }
                });

                //弹窗销毁事件
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        screenView.setVisibility(View.GONE);
                    }
                });
            }
        });


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println(context);
                String action = intent.getAction();
                if ("ACTION".equals(action)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openDB(view.getContext());
                            t_pay = getTPay(view.getContext());
                            t_surplus = m_superpay-t_pay;
                            tv_t_pay.setText(String.format("%.2f",t_pay));
                            tv_t_surplus.setText(String.format("%.2f",t_surplus));
                            Log.d("111111111","2");
                        }
                    },100);
                    ;

                }
            }
        };

        IntentFilter filter = new IntentFilter("ACTION");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        return view;
    }

    //获取当前月份的天数
    private int coculateDayByMonth(){
        int[] dayue = {1,3,5,7,8,10,12};
        int[] xiaoyue = {4,6,9,11};
        LocalDate today = LocalDate.now();
        int mon = today.getMonthValue();

        if (Arrays.asList(dayue).contains(mon)){
            return 31;
        } else if (Arrays.asList(xiaoyue).contains(mon)) {
            return 30;
        } else{
            int year = today.getYear();
            if (year%4==0){
                return 29;
            }else return 28;
        }


    }
    //获取当前月份的中文大写
    private String getMonth(){
        LocalDate today = LocalDate.now();
        return today.getMonth().getDisplayName(TextStyle.FULL,Locale.CHINA);
    }
    //获取当月剩余天数（不包含当日）
    private int getRemainDay() {
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(today);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth(); // 获取当月的最后一天

        long remainingDays = ChronoUnit.DAYS.between(today.plusDays(1), lastDayOfMonth);
        System.out.println(remainingDays);
        //如果是最后一天，则返回下个月的总天数
        if ((int) remainingDays==-1){
            return (int) yearMonth.plusMonths(1).lengthOfMonth();
        }
        return (int) remainingDays+1;
    }
    //获取当前天数
    private int getNowDay(){
        LocalDate today = LocalDate.now();
        System.out.println(today.getDayOfMonth());
        return today.getDayOfMonth();
    }

    //获取当前天数
    private String getStrNowDay(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        return today.format(formatter);
    }
    //返回年月形式
    private int getYM(){
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        return year*100+month;
    }

    //通过当前年月获取对应的数据
    @SuppressLint("Range")
    private void openDB(Context context){
        MydataHelper dbHelper = new MydataHelper(context);

        int ym = getYM();
        Log.d("------p1_getYM-------", String.valueOf(ym));

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


        // 判断 Cursor 是否为空或是否包含数据
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double income = cursor.getDouble(cursor.getColumnIndex("income"));
                double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                double budget = cursor.getDouble(cursor.getColumnIndex("budget"));

                updateMonViews(income, pay, budget);

                // 输出数据到日志
                Log.d("---page1_dbhelper-----", "/" + id + "/" + income + "/" + pay + "/" + budget);
            } while (cursor.moveToNext());
        } else {
            // 如果没有找到数据，可以进行相应的处理
            dbHelper.insertOverview(ym,0.00,0.00,0.00);
            Log.d("---page1_dbhelper-----", "No data found for id: " + ym);
            // 可以在这里添加默认值或其他处理逻辑
        }
        if (cursor != null){
            cursor.close();
        }

        // 关闭数据库
        dbHelper.close();
    }

    private void updateMonViews(double income, double pay, double budget){
        m_pay=pay;
        m_income=income;
        m_budget=budget;
        m_surplus=m_income-m_pay;
        m_perpay = m_pay/getNowDay();
        m_superpay = (m_budget-m_pay)/getRemainDay();//以预算为基准计算日结余

        tv_m_pay.setText(String.format("%.2f",m_pay));
        tv_m_income.setText(String.format("%.2f",m_income));
        tv_m_budget.setText(String.format("%.2f",m_budget));
        tv_m_surplus.setText(String.format("%.2f",m_surplus));//月结余
        tv_m_perpay.setText(String.format("%.2f",m_perpay));//月日均消费
        tv_m_superpay.setText(String.format("%.2f",m_superpay));
    }

    @SuppressLint("Range")
    private double getTPay(Context context){
        double tpay = 0.00;

        MydataHelper dbHelper = new MydataHelper(context);

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
            Cursor cursor = db.rawQuery("SELECT * FROM sq" + getYM() + " WHERE id >= 1 ORDER BY id ASC", null);


            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        String t_day = "";
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex("id"));
                            String day = cursor.getString(cursor.getColumnIndex("day"));
                            String min = cursor.getString(cursor.getColumnIndex("min"));
                            double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                            String type = cursor.getString(cursor.getColumnIndex("type"));
                            String remark = cursor.getString(cursor.getColumnIndex("remark"));
                            String IO = cursor.getString(cursor.getColumnIndex("io"));

                            if (day.equals(getStrNowDay()) && IO.equals("out")){
                                tpay+=pay;
                            }


                        } while (cursor.moveToNext());
                    }
                } finally {
                    // 关闭游标
                    cursor.close();
                }
            }
        } catch (Exception e){
            Log.d("exception",e.toString());
        }


        return tpay;

    }


}