package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewGroupActivity extends AppCompatActivity {
    EditText et_group_name;
    EditText et_radius;
    Button btn_create_group;
    Spinner sp_type;
    ArrayAdapter<CharSequence> adapter;
    //ArrayAdapter<String> adapter;
    Serialization serialize;
    String to_send;
    private static final String TAG = "NewGroupActivity";
    Boolean checkForRecv;
    groupType type;

    Toast toast;
    TextView tv;
    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        et_group_name = (EditText) findViewById(R.id.et_group_name);
        et_group_name.requestFocus();//put cursor on this edit text first (because id didn't do it defultly)
        et_radius = (EditText) findViewById(R.id.et_radius);
        btn_create_group = (Button) findViewById(R.id.btn_create_group);
        sp_type = (Spinner) findViewById(R.id.sp_type);
        to_send = "";
        checkForRecv = true;
        //type = groupType.Static;//defult

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        tv = new TextView(getApplicationContext());
        //tv.setBackgroundColor(Color.CYAN);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20);
        tf = Typeface.create("david", Typeface.BOLD);
        tv.setTypeface(tf);
        tv.setPadding(10, 10, 10, 10);



        adapter = ArrayAdapter.createFromResource(this,R.array.group_types,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        /*adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.my_spinner_style,R.array.group_types) {

            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);

                return v;

            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = super.getDropDownView(position, convertView, parent);

                ((TextView) v).setGravity(Gravity.CENTER);

                return v;
            }
        };*/
        sp_type.setAdapter(adapter);


        sp_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //do it when the user press on the button - if he did not
                // choose static or dynamic make atoast to tell him and do not
                // create the group/if(adapterView.getItemAtPosition(i).equals(static)){}
                Log.d(TAG, "onItemSelected() called with: adapterView = [" + adapterView + "], view = [" + view + "], i = [" + i + "], l = [" + l + "]");
                if(adapterView.getItemAtPosition(i).equals("Static")){
                    type = groupType.Static;
                }
                else if(adapterView.getItemAtPosition(i).equals("Dynamic")){
                    type = groupType.Dynamic;
                }
                else{
                    //if the user chose the thired option - click on the butten will not creat a group.
                    type = null;
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        btn_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type!=null){
                    Location group_loc = User.getUserLocation();
                    Log.d(TAG, "group_loc: " + group_loc.toString());
                    String group_name = et_group_name.getText().toString();
                    double radius = Double.parseDouble(et_radius.getText().toString())/1000;
                    Log.d(TAG, "radius=" + String.valueOf(radius));
                    String managerId = User.getUserName();
                    //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    @SuppressLint("SimpleDateFormat") String time_created = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    String picData = "-";
                    int int_type = 0; //static
                    if (type ==groupType.Dynamic)
                        int_type = 1;
                    to_send = "new_manager_group" + "%" +String.valueOf(int_type) + "&" + group_loc.toString() + "&" + String.valueOf(radius) + "&" + group_name + "&" + picData + "&" + managerId + "&" + time_created;
                    ClientTask.queToSend.add(to_send);

                    tv.setText("new group! - \""+group_name + "\"");
                    toast.setView(tv);
                    toast.show();
                    //now we need to come back to the tabbed activity
                    Intent tabbedIntent = new Intent(NewGroupActivity.this, TabbedMainActivity.class);
                    tabbedIntent.putExtra("caller", "NewGroupActivity");
                    NewGroupActivity.this.startActivity(tabbedIntent);



                    //the server will add this user automatically//OtherUser this_other_user = new OtherUser(User.getUserName(),User.getMd5Password(),User.getUserLocation(),User.getFirstName(),User.getLastName(),User.getAddress(),User.getGender(),User.getPhoneNum(),User);
                    //when the client here parse the data , he add the users to the group

                    //tab activity is responsible to add the group to the groups and to receive the server packets about everything!!!

                }
                else{
                    tv.setText("try again");
                    toast.setView(tv);
                    toast.show();
                }
            }
        });

    }



}
