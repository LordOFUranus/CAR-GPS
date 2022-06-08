package com.sattazalyk.car_gps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class activity_login extends FragmentActivity {

    //ui

    private EditText edt_iid, edt_pass;
    private Button btn_enter, btn_registration;

    DBHandler dbHandler = new DBHandler(activity_login.this);

    //user

    String iid;
    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        edt_iid = findViewById(R.id.edt_iid);
        edt_pass = findViewById(R.id.edt_pass);
        btn_enter = findViewById(R.id.btn_entere);
        btn_registration = findViewById(R.id.btn_registration);

        btn_enter.setOnClickListener(view -> {
            Account account = new Account();

            String iid = edt_iid.getText().toString().trim();
            String pass = edt_pass.getText().toString().trim();


            if(!iid.equals("") && !pass.equals("")){
                Boolean checkAccount = dbHandler.checkAccountPass(iid, pass);
                if(checkAccount == true){
                    Toast.makeText(this,"Вы вошли в приложение",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MapsActivity.class);
                    startActivity(intent);
                }
            }
            else{
                Toast.makeText(activity_login.this, "Заполните формы!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void registrationOnClick(View v){
        if(v.getId()==R.id.btn_registration){
            Intent intent = new Intent(this,RegistrationActivity.class);
            startActivity(intent);
        }
    }
}