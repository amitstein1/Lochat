package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RegisterActivity extends AppCompatActivity {

    EditText et_username_register;
    EditText et_password_register;
    EditText et_fname_register;
    EditText et_lname_register;
    EditText et_address_register;
    Button btnRgegister;


    Serialization serialize;
    String to_send;

    Boolean checkForRecv;

    Toast toast;
    TextView tv;
    Typeface tf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d("client_sock_register", "RegisterActivity");

        et_username_register = (EditText) findViewById(R.id.et_username_register);
        et_password_register = (EditText) findViewById(R.id.et_password_register);
        et_fname_register = (EditText) findViewById(R.id.et_fname_register);
        et_lname_register = (EditText) findViewById(R.id.et_lname_register);
        et_address_register = (EditText) findViewById(R.id.et_address_register);
        btnRgegister = (Button) findViewById(R.id.btn_register);
        //register automaticlly
        /*et_username_register = (EditText) findViewById(R.id.et_username_register);
        et_username_register.setText("a");
        et_password_register = (EditText) findViewById(R.id.et_password_register);
        et_password_register.setText("a");*/


        to_send = "";
        checkForRecv = true;

        serialize = new Serialization();

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        tv = new TextView(getApplicationContext());
        //tv.setBackgroundColor(Color.CYAN);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20);
        tf = Typeface.create("david", Typeface.BOLD);
        tv.setTypeface(tf);
        tv.setPadding(10, 10, 10, 10);

        btnRgegister.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Log.d("client_sock_register", "clicked on register now");
                if (!et_fname_register.getText().toString().equals("") && !et_lname_register.getText().toString().equals("") && !et_username_register.getText().toString().equals("")&& !et_password_register.getText().toString().equals("")&& !et_address_register.getText().toString().equals("")) {
                    String action = "register";
                    String username = et_username_register.getText().toString();
                    String md5_pw = getMd5Encryption(et_password_register.getText().toString());
                    String location = "32.1865231|34.891407";//does not matter - just a defult for now
                    String fname = et_fname_register.getText().toString();
                    String lname = et_lname_register.getText().toString();
                    String address = et_address_register.getText().toString();
                    String phone_num = "0544413762";//did not check
                    String email = "steinamiti@gmail.com";//did not check
                    String gender = "1";//did not check
                    String pic_data = "aefsd5f4sa6dfasdf54asdfsdf54sdf";//did not check
                    to_send = action + "%" + username + "~" + md5_pw + "~" + location + "~" + fname + "~" + lname + "~" + address + "~" + phone_num
                            + "~" + email + "~" + gender + "~" + pic_data;
                    ClientTask.queToSend.add(to_send);
                    Log.d("client_sock_register", "data was added to queToSend:" + to_send);
                }
                else{
                    tv.setText("fill all the fields");
                    toast.setView(tv);
                    toast.show();
                }
            }
        });

        handleServerMessages();

    }


    public String getMd5Encryption(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (
                NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(password.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString(); //Digest in hex format

    }

    public void goToLoginActivity() {
        checkForRecv = false;
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        //String[] queToSendArray = queToSend.toArray(new String[queToSend.size()]);
        //String[] queRecvArray = queRecv.toArray(new String[queRecv.size()]);
        //mapIntent.putExtra("queToSendArray", queToSendArray);
        //mapIntent.putExtra("queRecvArray", queRecvArray);
        RegisterActivity.this.startActivity(loginIntent);
    }

    //to manage back clicks
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            goToLoginActivity();//so we also do: checkForRecv = false; in addition to nevigating back
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void handleServerMessages() {
        Log.d("client_sock_register", "handleServerMessages");
        Thread t = new Thread() {
            @Override
            public void run() {
                while (checkForRecv) {
                    try {
                        //Log.d("client_sock_register", "loop recv:"+ String.valueOf(ClientTask.queRecv));
                        Thread.sleep(1000);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {

                                if (!ClientTask.queRecv.isEmpty()) {
                                    String received_data = ClientTask.queRecv.remove();
                                    Log.d("client_sock_register", "que received: " + received_data);
                                    String action = received_data;
                                    switch (action) {
                                        case "registered_successfully":
                                            checkForRecv = false;
                                            Log.d("client_sock_register", "now going to LoginActivity");
                                            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            RegisterActivity.this.startActivity(loginIntent);
                                            break;
                                        case "registered_unsuccessfully_username_taken":
                                            tv.setText("username is taken - try again");
                                            toast.setView(tv);
                                            toast.show();

                                            et_username_register.setText("");
                                            et_password_register.setText("");
                                            et_fname_register.setText("");
                                            et_lname_register.setText("");
                                            et_address_register.setText("");

                                            break;
                                        default:
                                            Log.d("something wrong", "got into wrong else statement");
                                            break;
                                    }
                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        t.start();
    }
}
