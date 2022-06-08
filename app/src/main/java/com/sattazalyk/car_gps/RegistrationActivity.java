package com.sattazalyk.car_gps;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.nio.channels.AsynchronousChannelGroup;
import java.sql.*;
import java.time.Instant;

public class RegistrationActivity extends AppCompatActivity {

    //ui

    private EditText edt_fn, edt_ln, edt_iid, edt_phone, edt_pass;
    private Button btn_registration_end;
    private long regTime;

    DBHandler  dbHandler;

    Connection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_user);

        edt_fn = findViewById(R.id.edt_fn);
        edt_ln = findViewById(R.id.edt_ln);
        edt_iid = findViewById(R.id.edt_iid);
        edt_phone = findViewById(R.id.edt_phone);
        edt_pass = findViewById(R.id.edt_pass);
        regTime = System.currentTimeMillis()/1000l;

        btn_registration_end = findViewById(R.id.btn_registration);


    }

        public void onClick(View v){
            if(v.getId() == R.id.btn_registration_end){

                DBHandler dbHandler = new DBHandler(RegistrationActivity.this);
                if (edt_fn.toString()!="" && edt_ln.toString()!="" && edt_iid.toString()!="" && edt_phone.toString()!="" && edt_pass.toString()!=""){
                    Account account = new Account();
                    account.setFirst_name(edt_fn.getText().toString().trim());
                    account.setLast_name(edt_ln.getText().toString().trim());
                    account.setIid(edt_iid.getText().toString().trim());
                    account.setPhone(edt_phone.getText().toString().trim());
                    account.setPass(edt_pass.getText().toString().trim());
                    account.setReg_time(regTime);

                    if(edt_iid.getText().length()==12){
                        if(edt_pass.getText().length()==11){
                            dbHandler.addAccount(
                                    account.getFirst_name(),
                                    account.getLast_name(),
                                    account.getIid(),
                                    account.getPhone(),
                                    account.getPass(),
                                    account.getReg_time());
                            Toast.makeText(this, Long.toString(regTime), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, activity_login.class);
                            startActivity(intent);
                        }else Toast.makeText(this,"Казахстанский номер телефона содержит 11 цифр", Toast.LENGTH_SHORT).show();
                    }else Toast.makeText(this,"ИИН содержит 12 цифр", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(this, "Заполните поля", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(this, "BUTTON ERROR!?", Toast.LENGTH_SHORT).show();
        }

}




//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                DBHandler dbHandler = new DBHandler();
//                Connection connection = dbHandler.connectionClass();
//                Statement statement = null;
//                try {
//                    statement = connection.createStatement();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    statement.executeUpdate("INSERT INTO users("first name", "last_name", "iid", "phone","pass") "+ "VALUES("+
//                            "'"+edt_fn.getText()+"'"+" '"+edt_ln.getText()+"'"+" '"+edt_iid.getText()+"'"+" '"+edt_phone.getText()+"'"+" '"+edt_pass.getText()+"'");
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        });