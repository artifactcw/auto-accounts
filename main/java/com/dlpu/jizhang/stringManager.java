package com.dlpu.jizhang;

public class stringManager {
    private static String[] option_out = {"餐饮","零食","水果","交通","日用","运动","衣物","饰品","通讯",
            "住房","学习","娱乐","恋爱","旅行","烟酒","数码","医疗","书籍","礼物","彩票","捐赠","意外","浮亏"};
    private static String[] option_in = {"生活费","工资","兼职","礼金","理财","彩票","施舍","退款","意外"};


    public static String[] getOption_out() {
        return option_out;
    }

    public static String[] getOption_in() {
        return option_in;
    }
}
