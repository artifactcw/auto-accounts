package com.dlpu.jizhang;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * 数据库帮助类，用于管理SQLite数据库的创建、复制和打开操作
 */
public class MydataHelper extends SQLiteOpenHelper {
    // 包名
    private static String PACKAGE_NAME = "com.dlpu.jizhang";
    // 数据库存储路径
    private static String DB_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/" + PACKAGE_NAME + "/databases/";
    // 数据库文件名
    private static String DB_NAME = "bill";
    // 数据库对象
    private SQLiteDatabase db;
    // 上下文对象
    private final Context context;

    /**
     * 构造方法
     *
     * @param context 上下文对象
     */
    public MydataHelper(Context context) {
        // 调用父类构造方法
        super(context, DB_NAME, null, 1);
        this.context = context;

    }

    /**
     * 创建数据库的方法
     *
     * @throws IOException 如果发生I/O错误，则抛出此异常
     */
    public void createDB() throws IOException {
        // 获取可读数据库实例
        this.getReadableDatabase();
        try {
            // 复制数据库文件
            copyDB();
        } catch (IOException e) {
            // 如果复制数据库文件时发生错误，抛出异常
            throw new Error("Error copying database");
        }
    }

    /**
     * 复制数据库文件从assets目录到设备指定路径
     *
     * @throws IOException 如果发生I/O错误，则抛出此异常
     */
    public void copyDB() throws IOException {
        try {
            // 检查数据库文件是否已经存在
            File dbFile = new File(DB_PATH + DB_NAME);
            SharedPreferences sharedPreferences= context.getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
            int first = sharedPreferences.getInt("first",0);

            if (first==1) {
                return; // 数据库文件已存在，不复制
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("first",1);
            editor.apply();

            // 打开assets目录中的数据库文件作为输入流
            InputStream ip = context.getAssets().open(DB_NAME + ".db");
            Log.i("Input Stream....", ip + "");
            // 构造输出文件路径
            String op = DB_PATH + DB_NAME;
            // 创建输出流对象
            OutputStream output = new FileOutputStream(op);
            // 创建缓冲区
            byte[] buffer = new byte[1024];
            int length;
            // 循环读取输入流数据并写入输出流
            while ((length = ip.read(buffer)) > 0) {
                output.write(buffer, 0, length);
                Log.i("Content.... ", length + "");
            }
            // 刷新输出流
            output.flush();
            // 关闭输出流和输入流
            output.close();
            ip.close();
        } catch (IOException e) {
            // 捕获I/O异常并记录错误信息
            Log.v("error", e.toString());
        }
    }

    /**
     * 打开数据库的方法
     *
     * @throws SQLException 如果发生SQL错误，则抛出此异常
     */
    public void openDB() throws SQLException {
        // 构造数据库文件路径
        String myPath = DB_PATH + DB_NAME;
        // 打开数据库文件，获取数据库对象
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        Log.i("open DB......", db.toString());
    }

    /**
     * 关闭数据库的方法
     */
    @Override
    public synchronized void close() {
        // 如果数据库对象不为空，则关闭数据库
        if (db != null)
            db.close();
        // 调用父类的close方法
        super.close();
    }

    /**
     * 创建数据库的回调方法，此处为空实现
     *
     * @param db 数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * 数据库版本升级的回调方法，此处为空实现
     *
     * @param db         数据库对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    /**
     * 根据id查询数据
     * @param id 要查询的id
     * @return 查询到的数据，例如一个Cursor对象或者自定义的数据对象
     */
    public Cursor queryById(long id,String table) {

        Cursor cursor = db.query(
                table, // 替换为你的表名
                null, // 查询所有列，如果只需要特定列，可以列出列名
                "id = ?", // 查询条件，id为要查询的列名
                new String[]{String.valueOf(id)}, // 查询条件的参数
                null, // 不需要分组
                null, // 不需要过滤
                null // 不需要排序
        );
        return cursor;
    }


    public void createTableAndInsertData(String tableName, String day,String min, double pay, String type, String remark, String io) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 检查表是否存在
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = db.rawQuery(checkTableSQL, null);

        if (cursor.getCount() == 0) {
            // 表不存在，创建表
            String CREATE_TABLE_SQL = "CREATE TABLE " + tableName + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "day TEXT,"+
                    "min TEXT," +
                    "pay INTEGER," +
                    "type TEXT," +
                    "remark TEXT," +
                    "io TEXT" +
                    ")";
            db.execSQL(CREATE_TABLE_SQL);
        }

        cursor.close();

        // 插入数据
        ContentValues values = new ContentValues();
        values.put("day", day);
        values.put("min", min);
        values.put("pay", pay);
        values.put("type", type);
        values.put("remark", remark);
        values.put("io", io);

        long result = db.insert(tableName, null, values);

        if (result == -1) {
            // 插入失败
            Log.e("Database", "Failed to insert data into table: " + tableName);
        }
        db.close();

    }

    //更改overview表中的数据
    @SuppressLint("Range")
    public void changetablebases(int id,double num, int mode,String IO){
        boolean isChange = false;
        Cursor cursor = this.queryById(id, "overview");
        if (mode==0){
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
//                            int id = cursor.getInt(cursor.getColumnIndex("id"));
                            double income = cursor.getDouble(cursor.getColumnIndex("income"));
                            double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                            double budget = cursor.getDouble(cursor.getColumnIndex("budget"));

                            if (IO.equals("out")) {
                                pay += num;
                                isChange = true;
                            } else if (IO.equals("in")){
                                income+=num;
                                isChange = true;
                            }
                            updateOverview(id,income,pay,budget);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    // 关闭游标
                    cursor.close();
                }
            }
        }else if (mode==1){
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
//                            int id = cursor.getInt(cursor.getColumnIndex("id"));
                            double income = cursor.getDouble(cursor.getColumnIndex("income"));
                            double pay = cursor.getDouble(cursor.getColumnIndex("pay"));
                            double budget = cursor.getDouble(cursor.getColumnIndex("budget"));

                            if (IO.equals("out")) {
                                pay -= num;
                                isChange = true;
                            } else if (IO.equals("in")){
                                income -= num;
                                isChange = true;
                            }
                            updateOverview(id, income, pay, budget);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    // 关闭游标
                    cursor.close();
                }
            }
        }
        if (!isChange) {
            if (IO.equals("out")){
            insertOverview(id,0.0,num,0.0);
            } else {
                insertOverview(id,num,0.0,0.0);
            }
//            changetablebases(id, num, mode, IO);
        }
        return;
    }
        /**
         * 更新 overview 表中的数据
         * @param id 要更新的记录的 id
         * @param income 新的收入值
         * @param pay 新的支出值
         * @param budget 新的预算值
         * @return 更新成功的行数
         */
    public int updateOverview(int id, double income, double pay, double budget) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("income", income);
            values.put("pay", pay);
            values.put("budget", budget);

            int rowsAffected = db.update("overview", values, "id = ?", new String[]{String.valueOf(id)});
            db.close();
            return rowsAffected;
        }

    public void insertOverview(int id,double income,double pay,double budget){
        SQLiteDatabase db = this.getWritableDatabase();

        // 插入数据
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("income", income);
        values.put("pay", pay);
        values.put("budget",budget);
        long result = db.insert("overview", null, values);

        if (result == -1) {
            // 插入失败，可以处理错误
            Log.e("Database", "Failed to insert data into table: overview");
        }
        db.close();
    }

    /**
     * 更改overview表中的数据
     * @param id 数据id
     * @param budget 预算
     */
    @SuppressLint("Range")
    public void changeBudget(int id,double budget){
        Cursor cursor = this.queryById(id,"overview");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        double income = cursor.getDouble(cursor.getColumnIndex("income"));
                        double pay = cursor.getDouble(cursor.getColumnIndex("pay"));

                        updateOverview(id, income, pay, budget);
                    } while (cursor.moveToNext());
                }
            } finally {
                // 关闭游标
                cursor.close();
            }
        }
    }

    public void deleteAndUpdateIds(String TABLE_NAME, int idToDelete) {
        String COLUMN_ID = "id";
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // 删除指定 ID 的数据
            String DELETE_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
            db.execSQL(DELETE_QUERY, new Object[]{idToDelete});

            // 更新后续 ID
            String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " SET " + COLUMN_ID + " = " + COLUMN_ID + " - 1 WHERE " + COLUMN_ID + " > ?";
            db.execSQL(UPDATE_QUERY, new Object[]{idToDelete});

            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @SuppressLint("Range")
    public String getIOById(String table, int id){
        Cursor cursor = this.queryById(id,table);
        String IO="";
        if (cursor != null && cursor.moveToFirst()) {
            do {

                IO = cursor.getString(cursor.getColumnIndex("io"));

            } while (cursor.moveToNext());
        }
        return IO;
    }


}