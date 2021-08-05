package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stein on 25/01/2018.
 */

public class GroupsFragment extends Fragment {
    private static final String TAG = "GroupsFragment";
    ArrayList<String> groupsNames;
    Boolean toUpdateUserGroups;
    private final int sleepTimeSeconds = 1;
    private final int sleepTimeMiliSeconds = sleepTimeSeconds * 1000;
    ListView listView;
    private ArrayAdapter<String> adapter;
    EditText filter;
    Toast toast;
    TextView tv;
    Typeface tf;
    Thread t;

    private void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment, container, false);
        setHasOptionsMenu(true);
        hideSoftKeyboard();

        Log.d(TAG, "start...");
        return view;
    }


    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        hideSoftKeyboard();
    }*/
    /*
    private void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }*/


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.manu_groups_fragment, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.new_group) {
            toUpdateUserGroups = false;
            Intent newGroupIntent = new Intent(getActivity(), NewGroupActivity.class);
            getActivity().startActivity(newGroupIntent);

            return true;
        } else if (id == R.id.my_groups) {
            toUpdateUserGroups = false;//so we will not get the messages for the MyGroupsActivity which also get messages from the queue
            Intent myGroupsIntent = new Intent(getActivity(), MyGroupsActivity.class);
            getActivity().startActivity(myGroupsIntent);
            return true;
        } else if (id == R.id.hidden_groups) {
            toUpdateUserGroups = false;
            Intent hiddenGroupsIntent = new Intent(getActivity(), HiddenGroupsActivity.class);
            getActivity().startActivity(hiddenGroupsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "view created start...");

        listView = (ListView) view.findViewById(R.id.lv_user_groups);
        filter = (EditText) view.findViewById(R.id.searchFilter);
        toUpdateUserGroups = true;
        groupsNames = User.getListOfUserGroupsNames();//new ArrayList<String>();

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, groupsNames);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        tv = new TextView(getContext());
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
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                String groupName = adapter.getItem(position);
                String groupId = getGroupsIdByName(groupName);
                chatIntent.putExtra("groupName", groupName);
                chatIntent.putExtra("groupId", groupId);
                getActivity().startActivity(chatIntent);
            }
        });


        //if we use ContextMenu we cant use setOnItemLongClickListener because ContextMenu happens in a long click on an item
        /*
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getActivity(), "long click! - List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_groups_fragment, menu);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.hide_group_id:
                Log.d(TAG, "clicked on hide_group_id context button");
                String action = "hide_user_group";
                String groupId = getGroupsIdByName(groupsNames.get(info.position));
                String to_send = action + "%" + groupId;
                ClientTask.queToSend.add(to_send);
                Log.d(TAG, "data was sent to client task activity:" + to_send);
                tv.setText("group \"" + groupsNames.get(info.position) + "\" is now hidden");
                toast.setView(tv);
                toast.show();


                //groupsNames.remove(info.position);
                //adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);

        }
        //return super.onContextItemSelected(item);
    }

    // get Groups Id By Name from user groups (not hidden and not manage)
    public String getGroupsIdByName(String name) {
        for (Group group : User.userGroups.values()) {
            if (group.getName().equals(name)) {
                Log.d(TAG, "getGroupsIdByName " + name + ": " + group.getId());
                return group.getId();

            }
        }
        return null;
    }

    //update the adapter
    public void updateGroups() {
        Log.d(TAG, "11111");
        t = new Thread() {
            @Override
            public void run() {
                while (toUpdateUserGroups) {
                    try {
                        Log.d(TAG, "2222");
                        Thread.sleep(sleepTimeMiliSeconds);  //1000ms = 1 sec
                        // here you check the value of getActivity() and break up if needed
                        if (getActivity() == null) {
                            Log.d(TAG, "ChatActivity.this = null");
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "3333");
                                //do somthing every 1 sec
                                ArrayList<String> newGroupsNames = User.getListOfUserGroupsNames();
                                if (!groupsNames.equals(newGroupsNames)) {//change the groups only if needed
                                    groupsNames = newGroupsNames;
                                    adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, groupsNames);
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


    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        //we want the thread to join
        Log.d(TAG, "onDestroyView");
        toUpdateUserGroups = false;
        Log.d(TAG, "changes in map are unavailable");
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "thread of changing live map was joined");

    }*/
}

