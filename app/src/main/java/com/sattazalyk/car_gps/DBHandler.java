package com.sattazalyk.car_gps;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.sql.*;

public class DBHandler extends SQLiteOpenHelper {
    Connection connection;

    private Context context;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sat-taza.db";
    public static final String TABLE_CONTRACT = "accounts";

    public static final String ACCOUNT_ID = "_id";
    public static final String ACCOUNT_FIRST_NAME = "first_name";
    public static final String ACCOUNT_LAST_NAME = "last_name";
    public static final String ACCOUNT_IID = "iid";
    public static final String ACCOUNT_PHONE = "phone";
    public static final String ACCOUNT_PASS = "pass";
    public static final String ACCOUNT_CREATED_AT = "created_at";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE "+TABLE_CONTRACT+"("+ACCOUNT_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +ACCOUNT_FIRST_NAME+" TEXT NOT NULL, "
            +ACCOUNT_LAST_NAME+" TEXT NOT NULL, "
            +ACCOUNT_IID+" TEXT NOT NULL UNIQUE, "
            +ACCOUNT_PHONE+" TEXT NOT NULL UNIQUE, "
            +ACCOUNT_PASS+" TEXT NOT NULL, "
            +ACCOUNT_CREATED_AT+" TEXT NOT NULL); ";

    public DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_CONTRACT);
        onCreate(sqLiteDatabase);
    }

    public void addAccount(String firstName, String lastName, String iid, String phone, String pass, Long time){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ACCOUNT_FIRST_NAME, firstName);
        contentValues.put(ACCOUNT_LAST_NAME, lastName);
        contentValues.put(ACCOUNT_IID, iid);
        contentValues.put(ACCOUNT_PHONE, phone);
        contentValues.put(ACCOUNT_PASS, pass);
        contentValues.put(ACCOUNT_CREATED_AT,time);
        long result = sqLiteDatabase.insert(TABLE_CONTRACT, null,contentValues);

        if(result ==-1) Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        else Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
    }

    public boolean checkAccountIID(Account account){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_CONTRACT+" WHERE iid = ?", new String[]{account.getIid()});
        if(cursor.getCount()>0){
            return true;
        }
        else{
            Toast.makeText(context,"Нет такого пользователя", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public boolean checkAccountPass(String accountIDD, String accountPass){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *FROM "+TABLE_CONTRACT+" WHERE iid=? and pass = ?", new String[]{accountIDD,accountPass});
        if(cursor.getCount()>0){
            return true;
        }
        else{
            Toast.makeText(context,"Вы ввели неверные данные", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


}


//    public Connection connectionClass(){
//        String connectionURL;
//
//        try {
//            Class.forName("net.sourceforge.jtds.jdbc.Driver");
//            connectionURL="jdbc:jtds:sqlserver://"+DATABASE_HOST+":"+DATABASE_PORT+"/"+DATABASE_NAME;
//            connection = DriverManager.getConnection(connectionURL, DATABASE_LOGIN, DATABASE_PASS);
//        }
//        catch (SQLException | ClassNotFoundException e){
//
//            Log.d("sql_id","! " + e.getMessage());
//        }
//
//        return connection;
//    }