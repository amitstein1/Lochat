package com.example.stein.client_1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static Boolean is_first_time_for_LoginActivity_in_this_run = true;

    EditText et_password;
    EditText et_username;
    TextView registerLink;
    Button btn_login;
    User user;
    ClientTask client_task; //will use this object also in the functions here - or in another class
    String to_send;
    Boolean checkForRecv;
    Serialization serialize;

    Toast toast;
    TextView tv;
    Typeface tf;





    /*public LoginActivity(){
        Log.i(TAG, "LoginActivity: constractor - once");
        try {
            client_task = new ClientTask();//queToSend, queRecv); //the task who always get data and also send data
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "before client task executed");
        client_task.execute("");//call background
        Log.d(TAG, "client task executed");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "LoginActivity onCreate");

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        tv = new TextView(getApplicationContext());
        //tv.setBackgroundColor(Color.CYAN);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20);
        tf = Typeface.create("david", Typeface.BOLD);
        tv.setTypeface(tf);
        tv.setPadding(10, 10, 10, 10);


        et_password = (EditText) findViewById(R.id.et_password);
        //et_password.setText("a");
        et_username = (EditText) findViewById(R.id.et_username);
        //et_username.setText("a");
        registerLink = (TextView) findViewById(R.id.tw_register);
        btn_login = (Button) findViewById(R.id.btn_login);
        user = null;
        to_send = "";
        checkForRecv = true;
        serialize = new Serialization();

        //if you want to run a code only at the first run - after installing the app
        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            // <---- run your one time code here
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }*/
        if (is_first_time_for_LoginActivity_in_this_run) {
            try {
                client_task = new ClientTask();//queToSend, queRecv); //the task who always get data and also send data
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "before client task executed");
            client_task.execute("");//call background
            Log.d(TAG, "client task executed");
            is_first_time_for_LoginActivity_in_this_run = false;
        }


        handleServerMessages(); //when a message comes from the server - here we will get the message - clientTask is responsible to send


        //when the user
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForRecv = false;
                Log.d(TAG, "clicked on register button");
                //checkForRecv = false;
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                //String[] queToSendArray = ClientTask.queToSend.toArray(new String[ClientTask.queToSend.size()]);
                //String[] queRecvArray = ClientTask.queRecv.toArray(new String[ClientTask.queRecv.size()]);
                //registerIntent.putExtra("queToSendArray", queToSendArray);
                //registerIntent.putExtra("queRecvArray", queRecvArray);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "clicked on login button");
                String action = "sign_in";


                String password = getMd5Encryption(et_password.getText().toString());

                to_send = action + "%" + et_username.getText().toString() + "~" + password; //et_password.getText().toString();
                ClientTask.queToSend.add(to_send);
                Log.d(TAG, "data was sent:" + to_send);
            }

        });


    }

    public String getMd5Encryption(String password) {
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance("MD5");
        } catch (
                NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        md.update(password.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString(); //Digest in hex format

        //System.out.println("Digest(in hex format):: " + sb.toString());

        /*//convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (
                int i = 0;
                i < byteData.length; i++)

        {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        System.out.println("Digest(in hex format):: " + hexString.toString());*/
    }

    @Override
    public void finishAffinity() {
        super.finishAffinity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "clicked on Key Down ");
            checkForRecv = false;
            String action = "client_exit";
            String to_send = action + "%-";
            ClientTask.queToSend.add(to_send);
            finishAffinity();


            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void handleServerMessages() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (checkForRecv) {
                    try {
                        Thread.sleep(1000);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!ClientTask.queRecv.isEmpty()) {
                                    String received_data = ClientTask.queRecv.remove();
                                    Log.d(TAG, "que received:" + received_data);
                                    List<String> listMassage = Arrays.asList
                                            (received_data.split("%"));
                                    Log.d(TAG, "listMassage=" + listMassage);
                                    String action = listMassage.get(0);
                                    List<String> params = Arrays.asList(listMassage.get(1).split("~"));
                                    //listMassage.subList(1, listMassage.size());
                                    Log.d(TAG, "params=" + params);

                                    if (action.equals("signed_in_successfully")) {
                                        Log.d(TAG, "signed in successfully");
                                        //User.setUserName("");// not possible!!!!!!!! - there is not object yet
                                        user = serialize.getNewUser(params);
                                        Log.d(TAG, "after serializition");
                                        checkForRecv = false;
                                        if (isServicesOK()) {
                                            //go to MainActivityTabbed intent function with all of the
                                            //user parameters - are set in the static variable
                                            Log.d(TAG, "ready to use google services");

                                            goToTabbedMainActivity();
                                        } else {
                                            Log.d(TAG, "problem - ");
                                        }


                                    } else if (action.equals("signed_in_unsuccessfully")) {
                                        Log.d(TAG, "signed in unsuccessfully");

                                        tv.setText("try again");
                                        toast.setView(tv);
                                        toast.show();

                                        et_username.setText(""); // clear username field
                                        et_password.setText(""); //clear password field

                                    } else {
                                        Log.d(TAG, "got into wrong else statement");
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

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void goToTabbedMainActivity() {

        Intent tabbedMainActivityIntent = new Intent(LoginActivity.this, TabbedMainActivity.class);
        tabbedMainActivityIntent.putExtra("caller", "LoginActivity");
        LoginActivity.this.startActivity(tabbedMainActivityIntent);
    }


}
