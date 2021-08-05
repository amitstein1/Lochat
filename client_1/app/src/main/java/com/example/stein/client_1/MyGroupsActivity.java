package com.example.stein.client_1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyGroupsActivity extends AppCompatActivity {
    private static final String TAG = "MyGroupsActivity";
    ArrayList<String> groupsNames;
    Boolean toUpdateUserGroups;
    private final int sleepTimeSeconds = 1;
    private final int sleepTimeMiliSeconds = 11* 1000;
    ListView listView;
    private ArrayAdapter<String> adapter;
    EditText filter;
    Toast toast;
    TextView tv;
    Typeface tf;

    private void hideSoftKeyboard(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        Log.d(TAG,"start...");
        hideSoftKeyboard();

        listView = (ListView) findViewById(R.id.lv_my_groups);
        filter = (EditText) findViewById(R.id.searchFilter);
        toUpdateUserGroups = true;
        groupsNames = User.getListOfManageGroupsNames();

        adapter = new ArrayAdapter<>(MyGroupsActivity.this, android.R.layout.simple_list_item_1, groupsNames);
        listView.setAdapter(adapter);
        //registerForContextMenu(listView);



        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        tv = new TextView(getApplicationContext());
        //tv.setBackgroundColor(Color.CYAN);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20);
        tf = Typeface.create("david", Typeface.BOLD);
        tv.setTypeface(tf);
        tv.setPadding(10, 10, 10, 10);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                //go to chat activity
                Intent chatIntent = new Intent(MyGroupsActivity.this, ChatActivity.class);
                String groupName = adapter.getItem(position);
                String groupId = getMyGroupsIdByName(groupName);
                chatIntent.putExtra("groupId", groupId);
                MyGroupsActivity.this.startActivity(chatIntent);
            }
        });

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        updateGroups();

    }
    public String getMyGroupsIdByName(String name){
        for (Group group : User.manage_groups.values() ) {
            if (group.getName().equals(name)) {
                Log.d(TAG, "getGroupIdByName " + name + ": " + group.getId());
                return group.getId();

            }
        }
        return null;
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "clicked on Key Down ");
            toUpdateUserGroups = false;
            Intent tabbedIntent = new Intent(MyGroupsActivity.this, TabbedMainActivity.class);
            tabbedIntent.putExtra("caller", "MyGroupsActivity");
            MyGroupsActivity.this.startActivity(tabbedIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void updateGroups(){
        Thread t = new Thread() {
            @Override
            public void run() {
                while (toUpdateUserGroups) {
                    try {
                        Thread.sleep(sleepTimeMiliSeconds);  //1000ms = 1 sec
                        MyGroupsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //do somthing every 1 sec
                                ArrayList<String> newGroupsNames = User.getListOfManageGroupsNames();
                                if (!groupsNames.equals(newGroupsNames)){//change the groups only if needed
                                    groupsNames = newGroupsNames;
                                    adapter = new ArrayAdapter<>(MyGroupsActivity.this, android.R.layout.simple_list_item_1, groupsNames);
                                    listView.setAdapter(adapter);
                                    Log.d(TAG, "groups was changed");
                                }
                                Log.d(TAG, "groupsNames:" + String.valueOf(groupsNames));

                                //if (MyMapFragment.toMakeChangesInMap){
                                //   //not always - some times the user is not in the mao fragment
                                //    // so we do not want to do changes...
                                //add to que
                                //    liveLocationChangesToMap.add()
                                //}
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
