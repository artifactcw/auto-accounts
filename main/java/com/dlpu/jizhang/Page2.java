package com.dlpu.jizhang;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Page2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Page2 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tvAmount;
    private StringBuilder amountBuilder;
    private GridLayout gridLayout;
    private String p_type="";
    private String amountStr=null;
    private EditText editText;
    private String remark=null;
    private String IO = "out";

    private String[] option_out = {"餐饮","零食","水果","交通","日用","运动","衣服","通讯","住房","学习","娱乐","恋爱","旅行","烟酒","数码","医疗","书籍","礼物","彩票","捐赠"};
    private String[] option_in = {"生活费","工资","兼职","礼金","理财","施舍"};
    private RadioButton rb_out,rb_in;
    private TextView tvSelectedTime,tvSelectedDate;
    private String date="";

    public Page2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Page2.
     */
    // TODO: Rename and change types and number of parameters
    public static Page2 newInstance(String param1, String param2) {
        Page2 fragment = new Page2();
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
        View view = inflater.inflate(R.layout.fragment_page2, container, false);

        date = getYM()+getNowDay();

        tvAmount = view.findViewById(R.id.tvAmount);
        amountBuilder = new StringBuilder();
        gridLayout=view.findViewById(R.id.p2_grid);
        editText = view.findViewById(R.id.p2_remarks);

        // 初始化数字按钮点击事件
        initNumberButton(R.id.btn1, "1",view);
        initNumberButton(R.id.btn2, "2",view);
        initNumberButton(R.id.btn3, "3",view);
        initNumberButton(R.id.btn4, "4",view);
        initNumberButton(R.id.btn5, "5",view);
        initNumberButton(R.id.btn6, "6",view);
        initNumberButton(R.id.btn7, "7",view);
        initNumberButton(R.id.btn8, "8",view);
        initNumberButton(R.id.btn9, "9",view);
        initNumberButton(R.id.btn0, "0",view);

        // 初始化小数点按钮点击事件
        Button btnDot = view.findViewById(R.id.btnDot);
        btnDot.setOnClickListener(v -> {
            if (!amountBuilder.toString().contains(".")) {
                if (amountBuilder.length() == 0) {
                    amountBuilder.append("0");
                }
                amountBuilder.append(".");
                updateAmountDisplay();
            }
        });

        // 初始化退格按钮点击事件
        Button btnBackspace = view.findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(v -> {
            if (amountBuilder.length() > 0) {
                amountBuilder.deleteCharAt(amountBuilder.length() - 1);
                updateAmountDisplay();
            }
        });
        btnBackspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tvAmount.setText("0.00");
                amountBuilder = new StringBuilder();
                return false;
            }
        });

        addCheckBox(view,gridLayout,stringManager.getOption_out());

        //处理IO
        rb_in = view.findViewById(R.id.radio_in);
        rb_out = view.findViewById(R.id.radio_out);

        rb_out.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rb_in.setChecked(false);
                    IO = "out";
                    addCheckBox(view,gridLayout,stringManager.getOption_out());
                }
            }
        });
        rb_in.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rb_out.setChecked(false);
                    IO="in";
                    addCheckBox(view,gridLayout,stringManager.getOption_in());
                }
            }
        });

        //提交按钮
        Button bt_submit = view.findViewById(R.id.p2_submit);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remark = editText.getText().toString();
                String money = amountStr;

                System.out.println(p_type);
                System.out.println(remark);
                System.out.println(money);

                if (money!=null) {
                    if (!money.equals("")) {

                        MydataHelper dbHelper = new MydataHelper(getContext());

                        try {
                            dbHelper.createDB(); // 创建数据库（如果需要复制数据库文件）
                            dbHelper.openDB(); // 打开数据库
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (p_type.equals("")) {
                            p_type = "未知";
                        }
                        dbHelper.createTableAndInsertData("sq" + Integer.valueOf(date)/100, String.format("%02d",Integer.valueOf(date)%100),
                                String.valueOf(tvSelectedTime.getText()), Double.valueOf(money), p_type, remark, IO);
                        dbHelper.changetablebases(Integer.valueOf(date)/100, Double.valueOf(money), 0, IO);
                        tvAmount.setText("0.00");
                        amountBuilder = new StringBuilder();
                        editText.setText("");
                        Toast.makeText(getContext(),"已记录"+money,Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        Button btnSelectTime = view.findViewById(R.id.btnSelectTime);
        tvSelectedTime = view.findViewById(R.id.p2_time);
        tvSelectedTime.setText(getNowMin());

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format("%02d:%02d",hourOfDay,minute);
                        tvSelectedTime.setText(time);
                    }
                },getHour(),getMin()
                ,true);

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               timePickerDialog.show();
            }
        });


        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        tvSelectedDate = view.findViewById(R.id.p2_date);
        tvSelectedDate.setText(""+Integer.valueOf(getYM())/100+"日"+Integer.valueOf(getYM())%100+"月"+getNowDay()+"日");

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String t_date = String.format("%04d年%02d月%02d日",year,month+1,dayOfMonth);
                        date = String.format("%04d%02d%02d",year,month+1,dayOfMonth);
                        tvSelectedDate.setText(t_date);
                    }
                },getYM()/100,getYM()%100-1,Integer.valueOf(getNowDay()));

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        return view;
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
    private int getHour(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
        return Integer.valueOf(time.format(formatter));
    }

    private int getMin(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm");
        return Integer.valueOf(time.format(formatter));
    }

    // 初始化数字按钮点击事件
    private void initNumberButton(int buttonId, final String number,View view){
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            amountBuilder.append(number);
            updateAmountDisplay();
        });
    }

    // 更新金额显示
    private void updateAmountDisplay() {
        amountStr = amountBuilder.toString();
        if (amountStr.isEmpty()) {
            tvAmount.setText("0.00");
        } else {
            // 确保小数点后最多两位
            int dotIndex = amountStr.indexOf(".");
            if (dotIndex != -1 && amountStr.length() - dotIndex > 3) {
                amountStr = amountStr.substring(0, dotIndex + 3);
                amountBuilder.setLength(0);
                amountBuilder.append(amountStr);
            }

            tvAmount.setText(amountStr);
            // 格式化显示两位小数
            if (!amountStr.contains(".")) {
                amountStr += ".00";
            } else if (dotIndex == amountStr.length() - 1) {
                amountStr += "00";
            } else if (dotIndex == amountStr.length() - 2) {
                amountStr += "0";
            }

            System.out.println(amountStr);
        }
    }

    //添加单选框按钮
    private void addCheckBox(View v,GridLayout gridLayout,String[] options){
        gridLayout.removeAllViews();

        for (String s:options){
            //ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(),R.style.RadioButtonStyle);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(s); // 设置文本
            radioButton.setTextSize(15); // 设置文本大小
            radioButton.setGravity(Gravity.CENTER); // 设置文本居中
            radioButton.setBackgroundResource(R.drawable.check); // 设置背景
            radioButton.setButtonDrawable(null);
            GridLayout.LayoutParams layoutParams =new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            layoutParams.width=140;
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
//                    System.out.println(radio);
                }
            });

            // 添加到GridLayout
            gridLayout.addView(radioButton);
        }
    }


}